package evl.passes;

import java.util.Map;
import java.util.Set;

import common.ElementInfo;
import common.NameFactory;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.statement.bbend.CaseGoto;
import evl.statement.bbend.CaseGotoOpt;
import evl.statement.bbend.IfGoto;
import evl.expression.Expression;
import evl.expression.TypeCast;
import evl.expression.reference.Reference;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowSsaUsage;
import evl.knowledge.KnowledgeBase;
import evl.statement.normal.CallStmt;
import evl.statement.normal.SsaGenerator;
import evl.statement.Statement;
import evl.statement.normal.Assignment;
import evl.statement.normal.VarDefInitStmt;
import evl.statement.phi.PhiStmt;
import evl.traverser.VariableReplacer;
import evl.traverser.range.CaseEnumUpdater;
import evl.traverser.range.CaseRangeUpdater;
import evl.traverser.range.RangeGetter;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.EnumType;
import evl.type.base.Range;
import evl.variable.SsaVariable;
import java.math.BigInteger;

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
// functions return true if something changed
class Narrower extends DefTraverser<Void, Void> {

  private KnowledgeBase kb;
  private KnowSsaUsage ksu;
  private StmtUpdater su;

  public Narrower(KnowledgeBase kb) {
    super();
    this.kb = kb;
    ksu = kb.getEntry(KnowSsaUsage.class);
    su = new StmtUpdater(kb);
  }

  private Map<SsaVariable, Range> getRanges(Expression condition) {
    Map<SsaVariable, Range> varRange = RangeGetter.getRange(condition, kb);
    return varRange;
    // for (Variable var : varRange.keySet()) {
    // Range range = varRange.get(var);
    // if (param.containsKey(var)) {
    // range = Range.narrow(param.get(var), varRange.get(var));
    // }
    // param.put(var, range);
    // }
  }

  @Override
  protected Void visitCaseGoto(CaseGoto obj, Void param) {
    // TODO boolean should also be allowed
    // TODO check somewhere else if case values are disjunct

    if( !( obj.getCondition() instanceof Reference ) ) {
      RError.err(ErrorType.Hint, obj.getCondition().getInfo(), "can only do range analysis on local variables");
      return null;
    }
    Reference ref = (Reference) obj.getCondition();
    if( !ref.getOffset().isEmpty() || !( ref.getLink() instanceof SsaVariable ) ) {
      RError.err(ErrorType.Hint, obj.getCondition().getInfo(), "can only do range analysis on local variables");
      return null;
    }
    SsaVariable var = (SsaVariable) ref.getLink();

    if( var.getType().getRef() instanceof Range ) {
      gotoRange(obj, var);
    } else if( var.getType().getRef() instanceof EnumType ) {
      gotoEnum(obj, var);
    } else {
      throw new RuntimeException("not yet implemented: " + var.getType().getRef().getClass().getCanonicalName());
    }

    return null;
  }

  private void gotoEnum(CaseGoto obj, SsaVariable var) {
    EnumType varType = (EnumType) var.getType().getRef();

    for( CaseGotoOpt opt : obj.getOption() ) {
      EnumType newType = CaseEnumUpdater.process(opt.getValue(), kb);
      if( newType.isRealSubtype(varType) ){
        replace(opt.getDst(), var, newType);
      }
    }

    // TODO also update range for default case
  }

  private void gotoRange(CaseGoto obj, SsaVariable var) {
    Range varType = (Range) var.getType().getRef();
    for( CaseGotoOpt opt : obj.getOption() ) {
      Range newType = CaseRangeUpdater.process(opt.getValue(), kb);
      if( Range.leftIsSmallerEqual(newType, varType) && !Range.isEqual(newType, varType) ) {
        replace(opt.getDst(), var, newType);
      }
    }

    // TODO also update range for default case
  }

  @Override
  protected Void visitIfGoto(IfGoto obj, Void param) {
    Map<SsaVariable, Range> varRange = RangeGetter.getRange(obj.getCondition(), kb);

    for( SsaVariable var : varRange.keySet() ) {
      Range newType = varRange.get(var);
      Range varType = (Range) var.getType().getRef();
      if( Range.leftIsSmallerEqual(newType, varType) && !Range.isEqual(newType, varType) ) {
        replace(obj.getThenBlock(), var, newType);
      }
    }

    // TODO also for ELSE

    return null;
  }

  private void replace(BasicBlock startBb, SsaVariable var, Type newType) {
    assert ( startBb.getPhi().isEmpty() ); // if not true, we have to find a solution :(
    SsaVariable newVar = new SsaVariable(var.getInfo(), NameFactory.getNew(), new TypeRef(new ElementInfo(), newType));
    Expression initExpr = new TypeCast(var.getInfo(), new Reference(startBb.getInfo(), var), new TypeRef(new ElementInfo(), newType));
    VarDefInitStmt ass = new VarDefInitStmt(var.getInfo(), newVar, initExpr);
    startBb.getCode().add(0, ass);
    VariableReplacer.replace(startBb, 1, var, newVar);

    Set<Statement> use = ksu.get(newVar);
    for( Statement stmt : use ) {
      update(stmt);
    }
  }

  // FIXME a bit messy, clean it up
  // follow variable usage until no more range can be narrowed
  private void update(Statement stmt) {
    if( su.traverse(stmt, null) ) {
      if( stmt instanceof SsaGenerator ) {
        SsaVariable var = ( (SsaGenerator) stmt ).getVariable();
        Set<Statement> use = ksu.get(var);
        for( Statement substmt : use ) {
          update(substmt);
        }
      }
    }
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

  @Override
  protected Boolean visitVarDefInitStmt(VarDefInitStmt obj, Void param) {
    Type type = ExpressionTypeChecker.process(obj.getInit(), kb);
    if( !( type instanceof Range ) ) {
      return false;   //TODO also check booleans and enums?
    }
    Range rtype = (Range) type;
    rtype = Range.narrow(rtype, (Range) obj.getVariable().getType().getRef());
    rtype = kbi.getRangeType(rtype.getLow(), rtype.getHigh());  // get registred type
    if( obj.getVariable().getType().getRef() != rtype ) {
      obj.getVariable().getType().setRef(rtype);
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected Boolean visitPhiStmt(PhiStmt obj, Void param) {
    Type type = obj.getVariable().getType().getRef();
    if( !( type instanceof Range ) ) {
      return false;   //TODO also check booleans and enums?
    }

    BigInteger low = null;
    BigInteger high = null;

    for( BasicBlock in : obj.getInBB() ) {
      Range exprt = (Range) ExpressionTypeChecker.process(obj.getArg(in), kb);
      if( ( low == null ) || ( low.compareTo(exprt.getLow()) > 0 ) ) {
        low = exprt.getLow();
      }
      if( ( high == null ) || ( high.compareTo(exprt.getHigh()) < 0 ) ) {
        high = exprt.getHigh();
      }
    }

    Range rtype = kbi.getRangeType(low, high);

    if( obj.getVariable().getType().getRef() != rtype ) {
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
}
