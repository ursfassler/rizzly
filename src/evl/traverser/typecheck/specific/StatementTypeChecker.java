package evl.traverser.typecheck.specific;

import java.util.List;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.statement.Assignment;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseOptRange;
import evl.statement.CaseOptValue;
import evl.statement.CaseStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.ReturnExpr;
import evl.statement.ReturnVoid;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.statement.While;
import evl.traverser.typecheck.LeftIsContainerOfRightTest;
import evl.type.Type;
import evl.type.base.BooleanType;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

public class StatementTypeChecker extends NullTraverser<Void, Void> {
  private KnowledgeBase kb;
  private KnowBaseItem kbi;
  private Type funcReturn;

  public StatementTypeChecker(KnowledgeBase kb, Type funcReturn) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
    this.funcReturn = funcReturn;
  }

  public static void process(Statement obj, Type funcReturn, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, funcReturn);
    adder.traverse(obj, null);
  }

  public static void process(Variable obj, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, null);
    adder.traverse(obj, null);
  }

  private void visitList(List<? extends Evl> list, Void sym) {
    for (Evl itr : list) {
      visit(itr, sym);
    }
  }

  @Override
  protected Void visitDefault(Evl obj, Void sym) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, Void sym) {
    visit(obj.getVariable(), sym);
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Void sym) {
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Void sym) {
    Type ret = obj.getType();
    Type defType = ExpressionTypeChecker.process(obj.getDef(), kb);
    if (!LeftIsContainerOfRightTest.process(ret, defType, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + ret.getName() + " := " + defType.getName());
    }
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, Void sym) {
    return null;
  }

  @Override
  protected Void visitWhile(While obj, Void sym) {
    Type cond = ExpressionTypeChecker.process(obj.getCondition(), kb);
    if (!(cond instanceof BooleanType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Need boolean type, got: " + cond.getName());
    }
    visit(obj.getBody(), sym);
    return null;
  }

  @Override
  protected Void visitIf(IfStmt obj, Void sym) {
    for (IfOption opt : obj.getOption()) {
      Type cond = ExpressionTypeChecker.process(opt.getCondition(), kb);
      if (!(cond instanceof BooleanType)) {
        RError.err(ErrorType.Error, opt.getInfo(), "Need boolean type, got: " + cond.getName());
      }
      visit(opt.getCode(), sym);
    }
    visit(obj.getDefblock(), sym);
    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, Void sym) {
    Type cond = ExpressionTypeChecker.process(obj.getCondition(), kb);
    // TODO enumerator and boolean should also be allowed
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), cond, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Condition variable has to be an integer, got: " + cond.getName());
    }
    // TODO check somewhere if case values are disjunct
    visitItr(obj.getOption(), sym);
    visit(obj.getOtherwise(), sym);
    return null;
  }

  @Override
  protected Void visitCaseOpt(CaseOpt obj, Void sym) {
    visitItr(obj.getValue(), sym);
    visit(obj.getCode(), sym);
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, Void sym) {
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
  protected Void visitCaseOptValue(CaseOptValue obj, Void sym) {
    Type value = ExpressionTypeChecker.process(obj.getValue(), kb);
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), value, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer, got: " + value.getName());
    }
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, Void sym) {
    ExpressionTypeChecker.process(obj.getCall(), kb);
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, Void sym) {
    Type lhs = RefTypeChecker.process(obj.getLeft(), kb);
    Type rhs = ExpressionTypeChecker.process(obj.getRight(), kb);
    if (!LeftIsContainerOfRightTest.process(lhs, rhs, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + lhs.getName() + " := " + rhs.getName());
    }
    return null;
  }

  @Override
  protected Void visitBlock(Block obj, Void sym) {
    visitList(obj.getStatements(), sym);
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, Void sym) {
    Type ret = ExpressionTypeChecker.process(obj.getExpr(), kb);
    if (!LeftIsContainerOfRightTest.process(funcReturn, ret, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible to return: " + funcReturn.getName() + " := " + ret.getName());
    }
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, Void sym) {
    return null;
  }

}
