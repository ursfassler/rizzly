package evl.traverser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.ssa.PhiInserter;

import common.Designator;
import common.ElementInfo;

import evl.DefTraverser;
import evl.Evl;
import evl.copy.Relinker;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.knowledge.KnowStateVariableReadWrite;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.statement.Assignment;
import evl.statement.VarDefStmt;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;

//FIXME this class is unsafe as it does not check if a called function reads/writes a state variable (and we use the cached version)
//FIXME check if a statement calls a function which reads/writes a cached global variable and read/write them back
@Deprecated
public class StateVariableExtractor extends DefTraverser<Void, Void> {
  private KnowStateVariableReadWrite ksvrw;
  private KnowledgeBase kb;

  public StateVariableExtractor(KnowledgeBase kb) {
    super();
    this.kb = kb;
    ksvrw = kb.getEntry(KnowStateVariableReadWrite.class);
  }

  public static void process(Namespace classes, KnowledgeBase kb) {
    StateVariableExtractor cutter = new StateVariableExtractor(kb);
    cutter.traverse(classes, null);
  }

  @Override
  protected Void visit(Evl obj, Void param) {
    if (obj instanceof FuncWithBody) {
      doit((FuncWithBody) obj);
    } else {
      super.visit(obj, null);
    }
    return null;
  }

  private void doit(FuncWithBody func) {
    Set<StateVariable> reads = ksvrw.getReads(func);
    Set<StateVariable> writes = ksvrw.getWrites(func);

    Set<StateVariable> replace = new HashSet<StateVariable>();

    for (StateVariable var : reads) {
      if (PhiInserter.isScalar(var.getType().getRef())) {
        replace.add(var);
      }
    }
    for (StateVariable var : writes) {
      if (PhiInserter.isScalar(var.getType().getRef())) {
        replace.add(var);
      }
    }

    for (StateVariable var : replace) {
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

    { // relink to func variable
      Map<StateVariable, FuncVariable> map = new HashMap<StateVariable, FuncVariable>();
      map.put(var, ssa);
      Relinker.relink(func, map);
    }

    if (read) {
      Assignment load = new Assignment(info, new Reference(info, ssa), new Reference(info, var));
      func.getBody().getEntry().getCode().add(0, load);
    }
    VarDefStmt def = new VarDefStmt(info, ssa);
    func.getBody().getEntry().getCode().add(0, def);
    if (write) {
      Assignment store = new Assignment(info, new Reference(info, var),new Reference(info, ssa));
      func.getBody().getExit().getCode().add(store);
    }
  }

}
