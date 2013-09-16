package evl.traverser;

import evl.cfg.BasicBlock;
import evl.hfsm.Transition;
import evl.statement.phi.PhiStmt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.GraphHelper;
import util.SimpleGraph;
import util.ssa.PhiInserter;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.copy.Relinker;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.function.FunctionHeader;
import evl.knowledge.KnowStateVariableReadWrite;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.statement.Statement;
import evl.statement.normal.Assignment;
import evl.statement.normal.NormalStmt;
import evl.statement.normal.VarDefStmt;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;

//TODO do it for transitions

//FIXME what with expressions in phi?

/**
 * Replaces all state variables used in a function with a cached/local version of it. Writes/Reads them back whenever a
 * called function reads/writes a state variable.
 * 
 * @author urs
 * 
 */
public class StateVariableExtractor extends DefTraverser<Void, Void> {

  private KnowStateVariableReadWrite ksvrw;
  final private Map<StateVariable, FuncVariable> cache = new HashMap<StateVariable, FuncVariable>();

  public StateVariableExtractor(KnowledgeBase kb) {
    super();
    ksvrw = kb.getEntry(KnowStateVariableReadWrite.class);
  }

  public static void process(Namespace classes, KnowledgeBase kb) {
    StateVariableExtractor cutter = new StateVariableExtractor(kb);
    cutter.traverse(classes, null);
    FuncProtector protector = new FuncProtector(kb, cutter.cache);
    protector.traverse(classes, null);
  }

  @Override
  protected Void visit(Evl obj, Void param) {
    if( obj instanceof FuncWithBody ) {
      doit((FuncWithBody) obj);
    } else {
      super.visit(obj, null);
    }
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }
  
  

  private void doit(FuncWithBody func) {
    Set<StateVariable> reads = ksvrw.getReads(func);
    Set<StateVariable> writes = ksvrw.getWrites(func);

    Set<StateVariable> replace = new HashSet<StateVariable>();

    for( StateVariable var : reads ) {
      if( PhiInserter.isScalar(var.getType().getRef()) ) {
        replace.add(var);
      }
    }
    for( StateVariable var : writes ) {
      if( PhiInserter.isScalar(var.getType().getRef()) ) {
        replace.add(var);
      }
    }

    for( StateVariable var : replace ) {
      replaceVar(var, func, reads.contains(var), writes.contains(var));
    }
  }

  /**
   * 
   * @param var
   *          the variable to replace
   * @param func
   * @param read
   *          add code to read variable in
   * @param write
   *          add code to write variable out
   */
  private void replaceVar(StateVariable var, FuncWithBody func, boolean read, boolean write) {
    ElementInfo info = var.getInfo();

    FuncVariable ssa = new FuncVariable(info, var.getName() + Designator.NAME_SEP + "ssa", var.getType().copy());

    cache.put(var, ssa);

    { // relink to func variable
      Map<StateVariable, FuncVariable> map = new HashMap<StateVariable, FuncVariable>();
      map.put(var, ssa);
      Relinker.relink(func, map);
    }

    if( read ) {
      Assignment load = new Assignment(info, new Reference(info, ssa), new Reference(info, var));
      func.getBody().getEntry().getCode().add(0, load);
    }
    VarDefStmt def = new VarDefStmt(info, ssa);
    func.getBody().getEntry().getCode().add(0, def);
    if( write ) {
      Assignment store = new Assignment(info, new Reference(info, var), new Reference(info, ssa));
      func.getBody().getExit().getCode().add(store);
    }
  }
}
/**
 * Writes state variables back if a called function reads that variable. Reads state variables back in if a called
 * function writes that variable.
 * 
 * @author urs
 * 
 */
class FuncProtector extends StatementReplacer<FunctionHeader> {

  private KnowStateVariableReadWrite ksvrw;
  final private Map<FunctionHeader, Set<StateVariable>> writes = new HashMap<FunctionHeader, Set<StateVariable>>();
  final private Map<FunctionHeader, Set<StateVariable>> reads = new HashMap<FunctionHeader, Set<StateVariable>>();
  final private Map<StateVariable, FuncVariable> cache;

  public FuncProtector(KnowledgeBase kb, Map<StateVariable, FuncVariable> cache) {
    super();
    this.cache = cache;
    SimpleGraph<Evl> g = CallgraphMaker.make(kb.getRoot(), kb);
    GraphHelper.doTransitiveClosure(g);
    this.ksvrw = kb.getEntry(KnowStateVariableReadWrite.class);

    for( Evl caller : g.vertexSet() ) {
      if( caller instanceof FunctionHeader ) {
        Set<StateVariable> writes = new HashSet<StateVariable>(ksvrw.getWrites((FunctionHeader) caller));
        Set<StateVariable> reads = new HashSet<StateVariable>(ksvrw.getReads((FunctionHeader) caller));
        for( Evl callee : g.getOutVertices(caller) ) {
          if( callee instanceof FunctionHeader ) {
            writes.addAll(ksvrw.getWrites((FunctionHeader) callee));
            reads.addAll(ksvrw.getReads((FunctionHeader) callee));
          }
        }
        this.writes.put((FunctionHeader) caller, writes);
        this.reads.put((FunctionHeader) caller, reads);
      }
    }
  }

  @Override
  protected List<NormalStmt> visit(Evl obj, FunctionHeader param) {
    if( obj instanceof FunctionHeader ) {
      assert ( param == null );
      param = (FunctionHeader) obj;
    }
    return super.visit(obj, param);
  }

  public void process(FunctionHeader func) {
    traverse(func, func);
  }

  @Override
  protected List<NormalStmt> visitNormalStmt(NormalStmt obj, FunctionHeader param) {
    FunctionHeader callee = getCallee(obj);
    if( callee != null ) {
      ElementInfo info = obj.getInfo();

      Set<StateVariable> used = new HashSet<StateVariable>();
      used.addAll(ksvrw.getReads(param));
      used.addAll(ksvrw.getWrites(param));

      Set<StateVariable> writeBack = new HashSet<StateVariable>(reads.get(callee));
      writeBack.retainAll(used);
      // TODO only write back needed variables
      // TODO check if variable was ever written to

      Set<StateVariable> readBack = new HashSet<StateVariable>(writes.get(callee));
      readBack.retainAll(used);
      // TODO only read back needed variables
      // TODO check if variable is ever read again

      List<NormalStmt> ret = new ArrayList<NormalStmt>();

      for( StateVariable sv : writeBack ) {
        Assignment ass = new Assignment(info, new Reference(info, sv), new Reference(info, cache.get(sv)));
        ret.add(ass);
      }
      ret.add(obj);
      for( StateVariable sv : readBack ) {
        FuncVariable cached = cache.get(sv);
        if( ( obj instanceof Assignment ) && ( ( (Assignment) obj ).getLeft().getLink() == cached ) ) {
          // we do not reload a cached value if we overwrite the very same value as our call/assignment statement does
          // not very nice when we handle that case here, maybe we have a better idea in the future
          // see testcaase casual/StateVar2
        } else {
          Assignment ass = new Assignment(info, new Reference(info, cached), new Reference(info, sv));
          ret.add(ass);
        }
      }

      return ret;
    }
    return null;
  }

  private FunctionHeader getCallee(Statement stmt) {
    CalleeGetter<Void> getter = new CalleeGetter<Void>();

    Set<FunctionHeader> callees = new HashSet<FunctionHeader>();
    getter.traverse(stmt, callees);
    switch( callees.size() ) {
      case 0:
        return null;
      case 1:
        return callees.iterator().next();
      default:
        RError.err(ErrorType.Fatal, stmt.getInfo(), "In this phase is only one call per statement allowed");
        return null;
    }
  }

  @Override
  protected List<NormalStmt> visitPhi(PhiStmt phi, BasicBlock in, FunctionHeader param) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
