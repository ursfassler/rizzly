package evl.passes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import util.NumberSet;

import common.ElementInfo;
import common.NameFactory;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.expression.reference.Reference;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowParent;
import evl.knowledge.KnowSsaUsage;
import evl.knowledge.KnowledgeBase;
import evl.statement.Statement;
import evl.statement.bbend.CaseGoto;
import evl.statement.bbend.CaseGotoOpt;
import evl.statement.bbend.IfGoto;
import evl.statement.bbend.ReturnExpr;
import evl.statement.normal.Assignment;
import evl.statement.normal.CallStmt;
import evl.statement.normal.SsaGenerator;
import evl.statement.normal.TypeCast;
import evl.statement.normal.VarDefInitStmt;
import evl.statement.phi.PhiStmt;
import evl.traverser.ClassGetter;
import evl.traverser.VariableReplacer;
import evl.traverser.range.CaseEnumUpdater;
import evl.traverser.range.CaseRangeUpdater;
import evl.traverser.range.RangeGetter;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.EnumType;
import evl.type.base.NumSet;
import evl.variable.SsaVariable;

/**
 * Replaces variables after if/goto with a variable of a more narrow range
 * 
 * @author urs
 * 
 */
public class RangeNarrower extends DefTraverser<Void, Void> {

  private Narrower narrower;

  public RangeNarrower(KnowledgeBase kb) {
    super();
    this.narrower = new Narrower(kb);
  }

  public static void process(Evl evl, KnowledgeBase kb) {
    RangeNarrower narrower = new RangeNarrower(kb);
    narrower.traverse(evl, null);
  }

  @Override
  protected Void visitBasicBlockList(BasicBlockList obj, Void param) {
    narrower.traverse(obj, null);
    return null;
  }
}

// functions return true if something changed
class Narrower extends DefTraverser<Void, Void> {

  final private KnowledgeBase kb;
  final private KnowBaseItem kbi;
  final private KnowSsaUsage ksu;
  final private KnowParent kp;
  final private StmtUpdater su;

  public Narrower(KnowledgeBase kb) {
    super();
    this.kb = kb;
    ksu = kb.getEntry(KnowSsaUsage.class);
    kbi = kb.getEntry(KnowBaseItem.class);
    kp = kb.getEntry(KnowParent.class);
    su = new StmtUpdater(kb);
  }

  @Override
  protected Void visitCaseGoto(CaseGoto obj, Void param) {
    // TODO boolean should also be allowed
    // TODO check somewhere else if case values are disjunct

    if (!(obj.getCondition() instanceof Reference)) {
      RError.err(ErrorType.Hint, obj.getCondition().getInfo(), "can only do range analysis on local variables");
      return null;
    }
    Reference ref = (Reference) obj.getCondition();
    if (!ref.getOffset().isEmpty() || !(ref.getLink() instanceof SsaVariable)) {
      RError.err(ErrorType.Hint, obj.getCondition().getInfo(), "can only do range analysis on local variables");
      return null;
    }
    SsaVariable var = (SsaVariable) ref.getLink();

    if (var.getType().getRef() instanceof NumSet) {
      gotoRange(obj, var);
    } else if (var.getType().getRef() instanceof EnumType) {
      gotoEnum(obj, var);
    } else {
      throw new RuntimeException("not yet implemented: " + var.getType().getRef().getClass().getCanonicalName());
    }

    return null;
  }

  private void gotoEnum(CaseGoto obj, SsaVariable var) {
    EnumType varType = (EnumType) var.getType().getRef();

    for (CaseGotoOpt opt : obj.getOption()) {
      EnumType newType = CaseEnumUpdater.process(opt.getValue(), kb);
      if (newType.isRealSubtype(varType)) {
        replace(opt.getDst(), var, newType);
      }
    }

    // TODO also update range for default case
  }

  private void gotoRange(CaseGoto obj, SsaVariable var) {
    NumSet varType = (NumSet) var.getType().getRef();
    for (CaseGotoOpt opt : obj.getOption()) {
      NumSet newType = CaseRangeUpdater.process(opt.getValue(), kb);
      int cmp = newType.getNumbers().getNumberCount().compareTo(varType.getNumbers().getNumberCount());
      assert (cmp <= 0);
      if (cmp < 0) {
        replace(opt.getDst(), var, newType);
      }
    }

    // TODO also update range for default case
  }

  @Override
  protected Void visitIfGoto(IfGoto obj, Void param) {
    {
      Map<SsaVariable, NumSet> ranges = RangeGetter.getSmallerRangeFor(true, obj.getCondition(), kb);
      LinkedList<SsaVariable> keys = new LinkedList<SsaVariable>(ranges.keySet());
      Collections.sort(keys);
      for (SsaVariable var : keys) {
        NumSet newType = ranges.get(var);
        newType = kbi.getNumsetType(newType.getNumbers().getRanges());
        replace(obj.getThenBlock(), var, newType);
      }
    }
    {
      Map<SsaVariable, NumSet> ranges = RangeGetter.getSmallerRangeFor(false, obj.getCondition(), kb);
      LinkedList<SsaVariable> keys = new LinkedList<SsaVariable>(ranges.keySet());
      Collections.sort(keys);
      for (SsaVariable var : keys) {
        NumSet newType = ranges.get(var);
        newType = kbi.getNumsetType(newType.getNumbers().getRanges());
        replace(obj.getElseBlock(), var, newType);
      }
    }
    return null;
  }

  private void replace(BasicBlock startBb, SsaVariable var, Type newType) {
    assert (kp.getParent(newType) != null);

    List<Reference> refs = ClassGetter.getAll(Reference.class, startBb.getPhi());
    for (Reference ref : refs) {
      assert (ref.getLink() != var); // if not true, we have to find a solution :( => insert a new basic block with
                                     // typecast
    }

    SsaVariable newVar = new SsaVariable(var.getInfo(), NameFactory.getNew(), new TypeRef(new ElementInfo(), newType));
    TypeCast initExpr = new TypeCast(var.getInfo(), newVar, new TypeRef(new ElementInfo(), newType), new Reference(startBb.getInfo(), var));
    startBb.getCode().add(0, initExpr);
    VariableReplacer.replace(startBb, 1, var, newVar);

    List<Statement> use = ksu.get(newVar);
    for (Statement stmt : use) {
      update(stmt);
    }
  }

  // FIXME a bit messy, clean it up
  // follow variable usage until no more range can be narrowed
  private void update(Statement stmt) {
    if (su.traverse(stmt, null)) {
      if (stmt instanceof SsaGenerator) {
        SsaVariable var = ((SsaGenerator) stmt).getVariable();
        List<Statement> use = ksu.get(var);
        for (Statement substmt : use) {
          update(substmt);
        }
      }
    }
  }

  // introduced by TransitionGuardNarrower
  @Override
  protected Void visitTypeCast(TypeCast obj, Void param) {
    List<Statement> use = ksu.get(obj.getVariable());
    for (Statement stmt : use) {
      update(stmt);
    }
    return super.visitTypeCast(obj, param);
  }

  @Override
  protected Void visitVarDefInitStmt(VarDefInitStmt obj, Void param) {
    update(obj);
    return null;
  }

}

// functions return true if something changed
class StmtUpdater extends NullTraverser<Boolean, Void> {

  private KnowledgeBase kb;
  private KnowBaseItem kbi;

  public StmtUpdater(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  @Override
  protected Boolean visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  private Boolean handleSsaGen(SsaGenerator obj, Type type) {
    if (!(type instanceof NumSet)) {
      return false; // TODO also check booleans and enums?
    }
    NumSet rtype = (NumSet) type;
    NumberSet rset = NumberSet.intersection(rtype.getNumbers(), ((NumSet) obj.getVariable().getType().getRef()).getNumbers());
    if (rset.isEmpty()) {
      return false; // TODO we could add a not reachable instruction?
    }
    rtype = kbi.getNumsetType(rset.getRanges()); // get registered type
    if (obj.getVariable().getType().getRef() != rtype) {
      obj.getVariable().getType().setRef(rtype);
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected Boolean visitVarDefInitStmt(VarDefInitStmt obj, Void param) {
    Type type = ExpressionTypeChecker.process(obj.getInit(), kb);
    return handleSsaGen(obj, type);
  }

  @Override
  protected Boolean visitTypeCast(TypeCast obj, Void param) {
    Type type = ExpressionTypeChecker.process(obj.getValue(), kb);
    return handleSsaGen(obj, type);
  }

  @Override
  protected Boolean visitPhiStmt(PhiStmt obj, Void param) {
    Type type = obj.getVariable().getType().getRef();
    if (!(type instanceof NumSet)) {
      return false; // TODO also check booleans and enums?
    }

    NumberSet retset = new NumberSet();

    for (BasicBlock in : obj.getInBB()) {
      NumSet exprt = (NumSet) ExpressionTypeChecker.process(obj.getArg(in), kb);
      retset = NumberSet.union(retset, exprt.getNumbers());
    }

    NumSet rtype = kbi.getNumsetType(retset.getRanges());

    if (obj.getVariable().getType().getRef() != rtype) {
      obj.getVariable().getType().setRef(rtype);
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected Boolean visitCallStmt(CallStmt obj, Void param) {
    // nothing to do since we do not produce a new value
    return false;
  }

  @Override
  protected Boolean visitAssignment(Assignment obj, Void param) {
    // nothing to do since we do not produce a new value
    return false;
  }

  @Override
  protected Boolean visitReturnExpr(ReturnExpr obj, Void param) {
    // nothing to do since we do not produce a new value
    return false;
  }

}
