package evl.traverser.typecheck.specific;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.ssa.PhiInserter;
import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.cfg.CaseGoto;
import evl.cfg.CaseGotoOpt;
import evl.cfg.CaseOptEntry;
import evl.cfg.CaseOptRange;
import evl.cfg.CaseOptValue;
import evl.cfg.Goto;
import evl.cfg.IfGoto;
import evl.cfg.PhiStmt;
import evl.cfg.ReturnExpr;
import evl.cfg.ReturnVoid;
import evl.expression.Expression;
import evl.expression.reference.Reference;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.statement.Assignment;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.statement.VarDefInitStmt;
import evl.statement.VarDefStmt;
import evl.traverser.range.CaseRangeUpdater;
import evl.traverser.range.RangeGetter;
import evl.traverser.typecheck.LeftIsContainerOfRightTest;
import evl.type.Type;
import evl.type.base.BooleanType;
import evl.type.base.Range;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

//TODO check return type at a different place
//TODO before this step, replace case statements with a boolean condition with an if statement
public class StatementTypeChecker extends NullTraverser<Void, Map<Variable, Range>> {
  private KnowledgeBase kb;
  private KnowBaseItem kbi;
  private Type funcReturn;
  private Map<BasicBlock, Map<Variable, Range>> map = new HashMap<BasicBlock, Map<Variable, Range>>();
  private Map<Variable, Range> ranges;

  public StatementTypeChecker(KnowledgeBase kb, Type funcReturn, Map<Variable, Range> ranges) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
    this.funcReturn = funcReturn;
    this.ranges = ranges;
  }

  public static void process(BasicBlockList obj, Type funcReturn, Map<Variable, Range> ranges, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, funcReturn, ranges);
    adder.traverse(obj, null);
  }

  public static void process(BasicBlockList obj, Type funcReturn, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, funcReturn, new HashMap<Variable, Range>());
    adder.traverse(obj, null);
  }

  public static void process(Statement obj, Type funcReturn, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, funcReturn, new HashMap<Variable, Range>());
    adder.traverse(obj, new HashMap<Variable, Range>());
  }

  public static void process(Variable obj, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, null, new HashMap<Variable, Range>());
    adder.traverse(obj, null);
  }

  private void visitList(List<? extends Evl> list, Map<Variable, Range> param) {
    for (Evl itr : list) {
      visit(itr, param);
    }
  }

  private void updateRange(Expression condition, Map<Variable, Range> param) {
    Map<Variable, Range> varRange = RangeGetter.getRange(condition, kb);
    for (Variable var : varRange.keySet()) {
      Range range = varRange.get(var);
      if (param.containsKey(var)) {
        range = Range.narrow(param.get(var), varRange.get(var));
      }
      param.put(var, range);
    }
  }

  private Range getRange(Variable var, List<CaseOptEntry> values) {
    Range r = CaseRangeUpdater.process(values, kb);
    return r;
  }

  @Override
  protected Void visitDefault(Evl obj, Map<Variable, Range> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, Map<Variable, Range> param) {
    visit(obj.getVariable(), param);

    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Map<Variable, Range> param) {
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Map<Variable, Range> param) {
    Type ret = obj.getType();
    Type defType = ExpressionTypeChecker.process(obj.getDef(), kb);
    if (!LeftIsContainerOfRightTest.process(ret, defType, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + ret.getName() + " := " + defType.getName());
    }
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, Map<Variable, Range> param) {
    return null;
  }

  @Override
  protected Void visitGoto(Goto obj, Map<Variable, Range> param) {
    return null;
  }

  @Override
  protected Void visitIfGoto(IfGoto obj, Map<Variable, Range> param) {
    Type cond = ExpressionTypeChecker.process(obj.getCondition(), kb);
    if (!(cond instanceof BooleanType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Need boolean type, got: " + cond.getName());
    }
    updateRange(obj.getCondition(), map.get(obj.getThenBlock()));
    // TODO also for ELSE

    return null;
  }

  @Override
  protected Void visitCaseGoto(CaseGoto obj, Map<Variable, Range> param) {
    Type cond = ExpressionTypeChecker.process(obj.getCondition(), kb);
    // TODO enumerator and boolean should also be allowed
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), cond, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Condition variable has to be an integer, got: " + cond.getName());
    }
    // TODO check somewhere if case values are disjunct

    if (obj.getCondition() instanceof Reference) {
      Reference ref = (Reference) obj.getCondition();
      if (ref.getOffset().isEmpty() && (ref.getLink() instanceof Variable) && (PhiInserter.isScalar(cond))) {
        Variable var = (Variable) ref.getLink();
        for (CaseGotoOpt opt : obj.getOption()) {
          Range r = getRange(var, opt.getValue());
          // TODO check that r is smaller as defined range of var
          map.get(opt.getDst()).put(var, r);
        }
      }
    }

    // TODO also update range for default case

    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, Map<Variable, Range> param) {
    Type start = ExpressionTypeChecker.process(obj.getStart(), kb);
    Type end = ExpressionTypeChecker.process(obj.getEnd(), kb);
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), start, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer (start), got: " + start.getName());
    }
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), end, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer (end), got: " + end.getName());
    }
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, Map<Variable, Range> param) {
    Type value = ExpressionTypeChecker.process(obj.getValue(), kb);
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), value, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer, got: " + value.getName());
    }
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, Map<Variable, Range> param) {
    ExpressionTypeChecker.process(obj.getCall(), kb);
    return null;
  }

  @Override
  protected Void visitPhiStmt(PhiStmt obj, Map<Variable, Range> param) {
    Type lhs = obj.getVariable().getType();
    for (BasicBlock in : obj.getInBB()) {
      Type rhs = obj.getArg(in).getType(); // TODO use map to get smaller range
      if (!LeftIsContainerOfRightTest.process(lhs, rhs, kb)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + lhs.getName() + " := " + rhs.getName());
      }
    }
    return null;
  }

  @Override
  protected Void visitVarDefInitStmt(VarDefInitStmt obj, Map<Variable, Range> param) {
    assert (param != null);
    Type lhs = obj.getVariable().getType();
    Type rhs = ExpressionTypeChecker.process(obj.getInit(), param, kb);
    if (!LeftIsContainerOfRightTest.process(lhs, rhs, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + lhs.getName() + " := " + rhs.getName());
    }
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, Map<Variable, Range> param) {
    Type lhs = RefTypeChecker.process(obj.getLeft(), kb);
    Type rhs = ExpressionTypeChecker.process(obj.getRight(), param, kb);
    if (!LeftIsContainerOfRightTest.process(lhs, rhs, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + lhs.getName() + " := " + rhs.getName());
    }
    return null;
  }

  @Override
  protected Void visitBasicBlockList(BasicBlockList obj, Map<Variable, Range> param) {
    for (BasicBlock bb : obj.getBasicBlocks()) {
      map.put(bb, new HashMap<Variable, Range>());
    }
    visitList(obj.getBasicBlocks(), null);
    return null;
  }

  @Override
  protected Void visitBasicBlock(BasicBlock obj, Map<Variable, Range> param) {
    assert (param == null);
    param = narrowAll(map.get(obj), ranges);
    visitList(obj.getPhi(), param);
    visitList(obj.getCode(), param);
    visit(obj.getEnd(), param);
    return null;
  }

  private Map<Variable, Range> narrowAll(Map<Variable, Range> a, Map<Variable, Range> b) {
    Map<Variable, Range> ret = new HashMap<Variable, Range>();
    ret.putAll(a);
    ret.putAll(b);
    Set<Variable> vars = new HashSet<Variable>(a.keySet());
    vars.retainAll(b.keySet());
    for (Variable var : vars) {
      ret.put(var, Range.narrow(a.get(var), b.get(var)));
    }
    return ret;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, Map<Variable, Range> param) {
    Type ret = ExpressionTypeChecker.process(obj.getExpr(), kb);
    if (!LeftIsContainerOfRightTest.process(funcReturn, ret, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible to return: " + funcReturn.getName() + " := " + ret.getName());
    }
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, Map<Variable, Range> param) {
    return null;
  }

}