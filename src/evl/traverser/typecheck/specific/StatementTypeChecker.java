package evl.traverser.typecheck.specific;

import java.util.HashMap;
import java.util.List;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.cfg.CaseOptRange;
import evl.cfg.CaseOptValue;
import evl.cfg.ReturnExpr;
import evl.cfg.ReturnVoid;
import evl.expression.Expression;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.statement.Assignment;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.traverser.RangeUpdater;
import evl.traverser.typecheck.LeftIsContainerOfRightTest;
import evl.type.Type;
import evl.type.base.BooleanType;
import evl.type.base.Range;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

public class StatementTypeChecker extends NullTraverser<Void, HashMap<Variable, Range>> {
  private KnowledgeBase kb;
  private KnowBaseItem kbi;
  private Type funcReturn;

  public StatementTypeChecker(KnowledgeBase kb, Type funcReturn) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
    this.funcReturn = funcReturn;
  }

  public static void process(BasicBlockList obj, Type funcReturn, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, funcReturn);
    adder.traverse(obj, new HashMap<Variable, Range>());
  }

  public static void process(Statement obj, Type funcReturn, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, funcReturn);
    adder.traverse(obj, new HashMap<Variable, Range>());
  }

  public static void process(Variable obj, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, null);
    adder.traverse(obj, null);
  }

  private void visitList(List<? extends Evl> list, HashMap<Variable, Range> sym) {
    for (Evl itr : list) {
      visit(itr, sym);
    }
  }

  private void updateRange(Expression condition, HashMap<Variable, Range> map) {
    RangeUpdater.process(condition, map, kb);
  }

  @Override
  protected Void visitDefault(Evl obj, HashMap<Variable, Range> sym) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, HashMap<Variable, Range> sym) {
    visit(obj.getVariable(), sym);

    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, HashMap<Variable, Range> sym) {
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, HashMap<Variable, Range> map) {
    Type ret = obj.getType();
    Type defType = ExpressionTypeChecker.process(obj.getDef(), kb);
    if (!LeftIsContainerOfRightTest.process(ret, defType, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + ret.getName() + " := " + defType.getName());
    }
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, HashMap<Variable, Range> map) {
    return null;
  }

  @Override
  protected Void visitWhile(While obj, HashMap<Variable, Range> map) {
    Type cond = ExpressionTypeChecker.process(obj.getCondition(), kb);
    if (!(cond instanceof BooleanType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Need boolean type, got: " + cond.getName());
    }
    map = new HashMap<Variable, Range>(map);
    updateRange(obj.getCondition(), map);
    visit(obj.getBody(), map);
    return null;
  }

  @Override
  protected Void visitIf(IfStmt obj, HashMap<Variable, Range> map) {
    for (IfOption opt : obj.getOption()) {
      Type cond = ExpressionTypeChecker.process(opt.getCondition(), kb);
      if (!(cond instanceof BooleanType)) {
        RError.err(ErrorType.Error, opt.getInfo(), "Need boolean type, got: " + cond.getName());
      }
      map = new HashMap<Variable, Range>(map);
      updateRange(opt.getCondition(), map);
      // TODO use knowledge from previous condition to constrain range of variable

      visit(opt.getCode(), map);
    }
    // TODO use knowledge from previous condition to constrain range of variable
    visit(obj.getDefblock(), map);
    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, HashMap<Variable, Range> map) {
    Type cond = ExpressionTypeChecker.process(obj.getCondition(), kb);
    // TODO enumerator and boolean should also be allowed
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), cond, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Condition variable has to be an integer, got: " + cond.getName());
    }
    // TODO check somewhere if case values are disjunct
    visitItr(obj.getOption(), map);
    visit(obj.getOtherwise(), map);
    return null;
  }

  @Override
  protected Void visitCaseOpt(CaseOpt obj, HashMap<Variable, Range> map) {
    visitItr(obj.getValue(), map);
    visit(obj.getCode(), map);
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, HashMap<Variable, Range> map) {
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
  protected Void visitCaseOptValue(CaseOptValue obj, HashMap<Variable, Range> map) {
    Type value = ExpressionTypeChecker.process(obj.getValue(), kb);
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), value, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer, got: " + value.getName());
    }
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, HashMap<Variable, Range> map) {
    ExpressionTypeChecker.process(obj.getCall(), kb);
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, HashMap<Variable, Range> map) {
    Type lhs = RefTypeChecker.process(obj.getLeft(), kb);
    Type rhs = ExpressionTypeChecker.process(obj.getRight(), map, kb);
    if (!LeftIsContainerOfRightTest.process(lhs, rhs, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + lhs.getName() + " := " + rhs.getName());
    }
    return null;
  }

  @Override
  protected Void visitBlock(Block obj, HashMap<Variable, Range> map) {
    visitList(obj.getStatements(), new HashMap<Variable, Range>(map));
    return null;
  }

  @Override
  protected Void visitBasicBlockList(BasicBlockList obj, HashMap<Variable, Range> map) {
    visitList(obj.getBasicBlocks(), new HashMap<Variable, Range>(map));
    return null;
  }

  @Override
  protected Void visitBasicBlock(BasicBlock obj, HashMap<Variable, Range> map) {
    map = new HashMap<Variable, Range>(map);
    visitList(obj.getPhi(), map);
    visitList(obj.getCode(), map);
    visit(obj.getEnd(), map);
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, HashMap<Variable, Range> map) {
    Type ret = ExpressionTypeChecker.process(obj.getExpr(), kb);
    if (!LeftIsContainerOfRightTest.process(funcReturn, ret, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible to return: " + funcReturn.getName() + " := " + ret.getName());
    }
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, HashMap<Variable, Range> map) {
    return null;
  }

}
