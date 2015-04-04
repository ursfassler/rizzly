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

package evl.traverser.other;

import java.util.List;

import error.ErrorType;
import error.RError;
import evl.data.component.hfsm.Transition;
import evl.data.expression.AnyValue;
import evl.data.expression.ArrayValue;
import evl.data.expression.BoolValue;
import evl.data.expression.Expression;
import evl.data.expression.NamedElementsValue;
import evl.data.expression.NamedValue;
import evl.data.expression.Number;
import evl.data.expression.RecordValue;
import evl.data.expression.StringValue;
import evl.data.expression.TupleValue;
import evl.data.expression.TypeCast;
import evl.data.expression.UnionValue;
import evl.data.expression.UnsafeUnionValue;
import evl.data.expression.binop.And;
import evl.data.expression.binop.BinaryExp;
import evl.data.expression.binop.BitAnd;
import evl.data.expression.binop.BitOr;
import evl.data.expression.binop.BitXor;
import evl.data.expression.binop.Div;
import evl.data.expression.binop.Equal;
import evl.data.expression.binop.Greater;
import evl.data.expression.binop.Greaterequal;
import evl.data.expression.binop.Is;
import evl.data.expression.binop.Less;
import evl.data.expression.binop.Lessequal;
import evl.data.expression.binop.LogicAnd;
import evl.data.expression.binop.LogicOr;
import evl.data.expression.binop.Minus;
import evl.data.expression.binop.Mod;
import evl.data.expression.binop.Mul;
import evl.data.expression.binop.Notequal;
import evl.data.expression.binop.Or;
import evl.data.expression.binop.Plus;
import evl.data.expression.binop.Shl;
import evl.data.expression.binop.Shr;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefIndex;
import evl.data.expression.reference.RefItem;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.expression.unop.BitNot;
import evl.data.expression.unop.LogicNot;
import evl.data.expression.unop.Not;
import evl.data.expression.unop.Uminus;
import evl.data.statement.AssignmentMulti;
import evl.data.statement.AssignmentSingle;
import evl.data.statement.CaseOptRange;
import evl.data.statement.CaseOptValue;
import evl.data.statement.IfOption;
import evl.data.statement.ReturnExpr;
import evl.data.statement.intern.MsgPush;
import evl.data.variable.DefVariable;
import evl.traverser.DefTraverser;

abstract public class ExprReplacer<T> extends DefTraverser<Expression, T> {

  protected <E extends Expression> void visitExprList(List<E> list, T param) {
    for (int i = 0; i < list.size(); i++) {
      Expression old = list.get(i);
      Expression expr = visit(old, param);
      if (expr == null) {
        RError.err(ErrorType.Fatal, old.getInfo(), "not handled class: " + old.getClass().getCanonicalName());
      }
      list.set(i, (E) expr);
    }
  }

  @Override
  protected Expression visitExpression(Expression obj, T param) {
    Expression ret = super.visitExpression(obj, param);
    assert (ret != null);
    return ret;
  }

  @Override
  protected Expression visitSimpleRef(SimpleRef obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitReference(Reference obj, T param) {
    for (RefItem item : obj.offset) {
      visit(item, param);
    }
    return obj;
  }

  @Override
  protected Expression visitRefIndex(RefIndex obj, T param) {
    obj.index = visit(obj.index, param);
    return null;
  }

  @Override
  protected Expression visitRefCall(RefCall obj, T param) {
    visitTupleValue(obj.actualParameter, param);
    return null;
  }

  @Override
  protected Expression visitRefName(RefName obj, T param) {
    return super.visitRefName(obj, param);
  }

  private Expression defaultBinaryOp(BinaryExp obj, T param) {
    obj.left = visit(obj.left, param);
    obj.right = visit(obj.right, param);
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
  protected Expression visitIs(Is obj, T param) {
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
  protected Expression visitBitXor(BitXor obj, T param) {
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
    obj.expr = visit(obj.expr, param);
    return obj;
  }

  @Override
  protected Expression visitLogicNot(LogicNot obj, T param) {
    obj.expr = visit(obj.expr, param);
    return obj;
  }

  @Override
  protected Expression visitBitNot(BitNot obj, T param) {
    obj.expr = visit(obj.expr, param);
    return obj;
  }

  @Override
  protected Expression visitUminus(Uminus obj, T param) {
    obj.expr = visit(obj.expr, param);
    return obj;
  }

  @Override
  protected Expression visitNumber(Number obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitStringValue(StringValue obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitArrayValue(ArrayValue obj, T param) {
    visitExprList(obj.value, param);
    return obj;
  }

  @Override
  protected Expression visitTupleValue(TupleValue obj, T param) {
    visitExprList(obj.value, param);
    return obj;
  }

  @Override
  protected Expression visitNamedElementsValue(NamedElementsValue obj, T param) {
    visitList(obj.value, param);
    return obj;
  }

  @Override
  protected Expression visitNamedValue(NamedValue obj, T param) {
    obj.value = visit(obj.value, param);
    return null;
  }

  @Override
  protected Expression visitUnionValue(UnionValue obj, T param) {
    super.visitUnionValue(obj, param);
    return obj;
  }

  @Override
  protected Expression visitUnsafeUnionValue(UnsafeUnionValue obj, T param) {
    super.visitUnsafeUnionValue(obj, param);
    return obj;
  }

  @Override
  protected Expression visitRecordValue(RecordValue obj, T param) {
    visitList(obj.value, param);
    return obj;
  }

  @Override
  protected Expression visitAnyValue(AnyValue obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitBoolValue(BoolValue obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitTypeCast(TypeCast obj, T param) {
    obj.value = visit(obj.value, param);
    return obj;
  }

  @Override
  protected Expression visitReturnExpr(ReturnExpr obj, T param) {
    obj.expr = visit(obj.expr, param);
    return null;
  }

  @Override
  protected Expression visitCaseOptValue(CaseOptValue obj, T param) {
    obj.value = visit(obj.value, param);
    return null;
  }

  @Override
  protected Expression visitCaseOptRange(CaseOptRange obj, T param) {
    obj.start = visit(obj.start, param);
    obj.end = visit(obj.end, param);
    return null;
  }

  @Override
  protected Expression visitDefVariable(DefVariable obj, T param) {
    obj.def = visit(obj.def, param);
    return null;
  }

  @Override
  protected Expression visitAssignmentMulti(AssignmentMulti obj, T param) {
    visitExprList(obj.left, param);
    obj.right = visit(obj.right, param);
    return null;
  }

  @Override
  protected Expression visitAssignmentSingle(AssignmentSingle obj, T param) {
    obj.left = (Reference) visit(obj.left, param);
    obj.right = visit(obj.right, param);
    return null;
  }

  @Override
  protected Expression visitTransition(Transition obj, T param) {
    obj.guard = visit(obj.guard, param);
    return super.visitTransition(obj, param);
  }

  @Override
  protected Expression visitIfOption(IfOption obj, T param) {
    obj.condition = visit(obj.condition, param);
    visit(obj.code, param);
    return null;
  }

  @Override
  protected Expression visitMsgPush(MsgPush obj, T param) {
    obj.queue = (Reference) visit(obj.queue, param);
    obj.func = (Reference) visit(obj.func, param);
    visitList(obj.data, param);
    return null;
  }

}
