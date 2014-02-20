package evl.traverser;

import java.util.List;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.RangeValue;
import evl.expression.StringValue;
import evl.expression.TypeCast;
import evl.expression.binop.And;
import evl.expression.binop.BinaryExp;
import evl.expression.binop.BitAnd;
import evl.expression.binop.BitOr;
import evl.expression.binop.Div;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.LogicAnd;
import evl.expression.binop.LogicOr;
import evl.expression.binop.Minus;
import evl.expression.binop.Mod;
import evl.expression.binop.Mul;
import evl.expression.binop.Notequal;
import evl.expression.binop.Or;
import evl.expression.binop.Plus;
import evl.expression.binop.Shl;
import evl.expression.binop.Shr;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.unop.BitNot;
import evl.expression.unop.LogicNot;
import evl.expression.unop.Not;
import evl.expression.unop.Uminus;
import evl.hfsm.Transition;
import evl.statement.Assignment;
import evl.statement.CaseOptRange;
import evl.statement.CaseOptValue;
import evl.statement.IfOption;
import evl.statement.ReturnExpr;
import evl.variable.Constant;

abstract public class ExprReplacer<T> extends DefTraverser<Expression, T> {

  protected void visitExprList(List<Expression> list, T param) {
    for (int i = 0; i < list.size(); i++) {
      Expression old = list.get(i);
      Expression expr = visit(old, param);
      if (expr == null) {
        RError.err(ErrorType.Fatal, old.getInfo(), "not handled class: " + old.getClass().getCanonicalName());
      }
      list.set(i, expr);
    }
  }

  @Override
  protected Expression visitExpression(Expression obj, T param) {
    Expression ret = super.visitExpression(obj, param);
    assert (ret != null);
    return ret;
  }

  @Override
  protected Expression visitReference(Reference obj, T param) {
    for (RefItem item : obj.getOffset()) {
      visit(item, param);
    }
    return obj;
  }

  @Override
  protected Expression visitRefIndex(RefIndex obj, T param) {
    obj.setIndex(visit(obj.getIndex(), param));
    return null;
  }

  @Override
  protected Expression visitRefCall(RefCall obj, T param) {
    visitExprList(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected Expression visitRefName(RefName obj, T param) {
    return super.visitRefName(obj, param);
  }

  private Expression defaultBinaryOp(BinaryExp obj, T param) {
    obj.setLeft(visit(obj.getLeft(), param));
    obj.setRight(visit(obj.getRight(), param));
    return obj;
  }

  @Override
  protected Expression visitAnd(And obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitDiv(Div obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitEqual(Equal obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitGreater(Greater obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitGreaterequal(Greaterequal obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitLess(Less obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitLessequal(Lessequal obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitMinus(Minus obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitMod(Mod obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitMul(Mul obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitNotequal(Notequal obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitOr(Or obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitPlus(Plus obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitShl(Shl obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitShr(Shr obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitBitAnd(BitAnd obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitBitOr(BitOr obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitLogicOr(LogicOr obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitLogicAnd(LogicAnd obj, T param) {
    return defaultBinaryOp(obj, param);
  }

  @Override
  protected Expression visitNot(Not obj, T param) {
    obj.setExpr(visit(obj.getExpr(), param));
    return obj;
  }

  @Override
  protected Expression visitLogicNot(LogicNot obj, T param) {
    obj.setExpr(visit(obj.getExpr(), param));
    return obj;
  }

  @Override
  protected Expression visitBitNot(BitNot obj, T param) {
    obj.setExpr(visit(obj.getExpr(), param));
    return obj;
  }

  @Override
  protected Expression visitUminus(Uminus obj, T param) {
    obj.setExpr(visit(obj.getExpr(), param));
    return obj;
  }

  @Override
  protected Expression visitNumber(Number obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitRangeValue(RangeValue obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitStringValue(StringValue obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitArrayValue(ArrayValue obj, T param) {
    visitExprList(obj.getValue(), param);
    return obj;
  }

  @Override
  protected Expression visitBoolValue(BoolValue obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitTypeCast(TypeCast obj, T param) {
    obj.setValue(visit(obj.getValue(), param));
    return obj;
  }

  @Override
  protected Expression visitReturnExpr(ReturnExpr obj, T param) {
    obj.setExpr(visit(obj.getExpr(), param));
    return null;
  }

  @Override
  protected Expression visitCaseOptValue(CaseOptValue obj, T param) {
    obj.setValue(visit(obj.getValue(), param));
    return null;
  }

  @Override
  protected Expression visitCaseOptRange(CaseOptRange obj, T param) {
    obj.setStart(visit(obj.getStart(), param));
    obj.setEnd(visit(obj.getEnd(), param));
    return null;
  }

  @Override
  protected Expression visitConstant(Constant obj, T param) {
    obj.setDef(visit(obj.getDef(), param));
    return null;
  }

  @Override
  protected Expression visitAssignment(Assignment obj, T param) {
    obj.setLeft((Reference) visit(obj.getLeft(), param));
    obj.setRight(visit(obj.getRight(), param));
    return null;
  }

  @Override
  protected Expression visitTransition(Transition obj, T param) {
    obj.setGuard(visit(obj.getGuard(), param));
    return super.visitTransition(obj, param);
  }

  @Override
  protected Expression visitIfOption(IfOption obj, T param) {
    obj.setCondition(visit(obj.getCondition(), param));
    visit(obj.getCode(), param);
    return null;
  }

}
