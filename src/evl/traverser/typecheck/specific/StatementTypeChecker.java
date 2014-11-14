/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package evl.traverser.typecheck.specific;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.Expression;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowType;
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
import evl.statement.WhileStmt;
import evl.statement.intern.MsgPush;
import evl.traverser.typecheck.LeftIsContainerOfRightTest;
import evl.type.Type;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.variable.ConstGlobal;
import evl.variable.ConstPrivate;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

public class StatementTypeChecker extends NullTraverser<Void, Void> {
  private KnowledgeBase kb;
  private KnowType kt;
  private KnowBaseItem kbi;
  private Type funcReturn;

  public StatementTypeChecker(KnowledgeBase kb, Type funcReturn) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
    kt = kb.getEntry(KnowType.class);
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

  private Type checkGetExpr(Expression expr) {
    ExpressionTypeChecker.process(expr, kb);
    return kt.get(expr);
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
  protected Void visitEnumElement(EnumElement obj, Void param) {
    return null;
  }

  private void checkConstant(Constant obj) {
    Type ret = obj.getType().getLink();
    Type defType = checkGetExpr(obj.getDef());
    if (!LeftIsContainerOfRightTest.process(ret, defType, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + ret.getName() + " := " + defType.getName());
    }
  }

  @Override
  protected Void visitConstPrivate(ConstPrivate obj, Void param) {
    checkConstant(obj);
    return null;
  }

  @Override
  protected Void visitConstGlobal(ConstGlobal obj, Void param) {
    checkConstant(obj);
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, Void map) {
    return null;
  }

  @Override
  protected Void visitWhileStmt(WhileStmt obj, Void param) {
    Type cond = checkGetExpr(obj.getCondition());
    if (!(cond instanceof BooleanType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Need boolean type, got: " + cond.getName());
    }
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Void visitIfStmt(IfStmt obj, Void param) {
    for (IfOption opt : obj.getOption()) {
      Type cond = checkGetExpr(opt.getCondition());
      if (!(cond instanceof BooleanType)) {
        RError.err(ErrorType.Error, opt.getInfo(), "Need boolean type, got: " + cond.getName());
      }
      visit(opt.getCode(), param);
    }
    visit(obj.getDefblock(), param);
    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, Void map) {
    Type cond = checkGetExpr(obj.getCondition());
    // TODO enumerator, union and boolean should also be allowed
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), cond, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Condition variable has to be an integer, got: " + cond.getName());
    }
    // TODO check somewhere if case values are disjunct
    visitList(obj.getOption(), map);
    visit(obj.getOtherwise(), map);
    return null;
  }

  @Override
  protected Void visitCaseOpt(CaseOpt obj, Void map) {
    visitList(obj.getValue(), map);
    visit(obj.getCode(), map);
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, Void map) {
    Type start = checkGetExpr(obj.getStart());
    Type end = checkGetExpr(obj.getEnd());
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), start, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer (start), got: " + start.getName());
    }
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), end, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer (end), got: " + end.getName());
    }
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, Void map) {
    Type value = checkGetExpr(obj.getValue());
    if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), value, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer, got: " + value.getName());
    }
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, Void map) {
    ExpressionTypeChecker.process(obj.getCall(), kb);
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, Void param) {
    Type lhs = checkGetExpr(obj.getLeft());
    Type rhs = checkGetExpr(obj.getRight());
    if (!LeftIsContainerOfRightTest.process(lhs, rhs, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + lhs.getName() + " := " + rhs.getName());
    }
    return null;
  }

  @Override
  protected Void visitBlock(Block obj, Void map) {
    visitList(obj.getStatements(), null);
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, Void map) {
    Type ret = checkGetExpr(obj.getExpr());
    if (!LeftIsContainerOfRightTest.process(funcReturn, ret, kb)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible to return: " + funcReturn.getName() + " := " + ret.getName());
    }
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, Void map) {
    return null;
  }

  @Override
  protected Void visitMsgPush(MsgPush obj, Void param) {
    Type func = checkGetExpr(obj.getFunc());
    // TODO implement check
    RError.err(ErrorType.Warning, obj.getInfo(), "Type check for push function not yet implemented");
    return null;
  }

}
