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

package ast.traverser.other;

import java.util.List;

import ast.data.AstList;
import ast.data.component.composition.CompUse;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateSimple;
import ast.data.component.hfsm.Transition;
import ast.data.expression.AnyValue;
import ast.data.expression.ArrayValue;
import ast.data.expression.BoolValue;
import ast.data.expression.Expression;
import ast.data.expression.NamedElementsValue;
import ast.data.expression.NamedValue;
import ast.data.expression.Number;
import ast.data.expression.RecordValue;
import ast.data.expression.StringValue;
import ast.data.expression.TupleValue;
import ast.data.expression.TypeCast;
import ast.data.expression.UnionValue;
import ast.data.expression.UnsafeUnionValue;
import ast.data.expression.binop.And;
import ast.data.expression.binop.BinaryExp;
import ast.data.expression.binop.BitAnd;
import ast.data.expression.binop.BitOr;
import ast.data.expression.binop.BitXor;
import ast.data.expression.binop.Div;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.Greaterequal;
import ast.data.expression.binop.Is;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.Lessequal;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.LogicOr;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Mod;
import ast.data.expression.binop.Mul;
import ast.data.expression.binop.Notequal;
import ast.data.expression.binop.Or;
import ast.data.expression.binop.Plus;
import ast.data.expression.binop.Shl;
import ast.data.expression.binop.Shr;
import ast.data.expression.reference.CompRef;
import ast.data.expression.reference.FuncRef;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.RefIndex;
import ast.data.expression.reference.RefItem;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.RefTemplCall;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.expression.reference.StateRef;
import ast.data.expression.reference.TypeRef;
import ast.data.expression.unop.BitNot;
import ast.data.expression.unop.LogicNot;
import ast.data.expression.unop.Not;
import ast.data.expression.unop.Uminus;
import ast.data.expression.unop.UnaryExp;
import ast.data.function.header.FuncProcedure;
import ast.data.function.ret.FuncReturnType;
import ast.data.statement.AssignmentMulti;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.CaseOptRange;
import ast.data.statement.CaseOptValue;
import ast.data.statement.CaseStmt;
import ast.data.statement.IfOption;
import ast.data.statement.MsgPush;
import ast.data.statement.ReturnExpr;
import ast.data.statement.VarDefInitStmt;
import ast.data.statement.WhileStmt;
import ast.data.template.ActualTemplateArgument;
import ast.data.type.composed.NamedElement;
import ast.data.variable.Constant;
import ast.data.variable.DefVariable;
import ast.data.variable.Variable;
import ast.traverser.DefTraverser;
import error.ErrorType;
import error.RError;

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
    if (ret == null) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "not handled class: " + obj.getClass().getCanonicalName());
    }
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

  @Override
  protected Expression visitRefTemplCall(RefTemplCall obj, T param) {
    AstList<ActualTemplateArgument> list = obj.actualParameter;
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i) instanceof Expression) {
        Expression old = (Expression) list.get(i);
        Expression expr = visit(old, param);
        list.set(i, expr);
      }
    }
    return null;
  }

  private Expression defaultBinaryOp(BinaryExp obj, T param) {
    obj.left = visit(obj.left, param);
    obj.right = visit(obj.right, param);
    return obj;
  }

  private Expression defaultUnaryOp(UnaryExp obj, T param) {
    obj.expr = visit(obj.expr, param);
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
    return defaultUnaryOp(obj, param);
  }

  @Override
  protected Expression visitLogicNot(LogicNot obj, T param) {
    return defaultUnaryOp(obj, param);
  }

  @Override
  protected Expression visitBitNot(BitNot obj, T param) {
    return defaultUnaryOp(obj, param);
  }

  @Override
  protected Expression visitUminus(Uminus obj, T param) {
    return defaultUnaryOp(obj, param);
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
    super.visitNamedElementsValue(obj, param);
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
    super.visitRecordValue(obj, param);
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
  protected Expression visitIfOption(IfOption obj, T param) {
    obj.condition = visit(obj.condition, param);
    visit(obj.code, param);
    return null;
  }

  @Override
  protected Expression visitMsgPush(MsgPush obj, T param) {
    obj.queue = (Reference) visit(obj.queue, param);
    obj.func = (Reference) visit(obj.func, param);
    visitExprList(obj.data, param);
    return null;
  }

  @Override
  protected Expression visitBoolValue(BoolValue obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitAnyValue(AnyValue obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitConstant(Constant obj, T param) {
    visit(obj.type, param);
    obj.def = visit(obj.def, param);
    return null;
  }

  @Override
  protected Expression visitVarDefInitStmt(VarDefInitStmt obj, T param) {
    obj.initial = visit(obj.initial, param);
    return super.visitVarDefInitStmt(obj, param);
  }

  @Override
  protected Expression visitCaseStmt(CaseStmt obj, T param) {
    obj.condition = visit(obj.condition, param);
    visit(obj.otherwise, param);
    return super.visitCaseStmt(obj, param);
  }

  @Override
  protected Expression visitWhileStmt(WhileStmt obj, T param) {
    obj.condition = visit(obj.condition, param);
    visit(obj.body, param);
    return null;
  }

  @Override
  protected Expression visitTransition(Transition obj, T param) {
    obj.src = (StateRef) visit(obj.src, param);
    obj.dst = (StateRef) visit(obj.dst, param);
    obj.eventFunc = (FuncRef) visit(obj.eventFunc, param);
    obj.guard = visit(obj.guard, param);
    return super.visitTransition(obj, param);
  }

  @Override
  protected Expression visitNamedElement(NamedElement obj, T param) {
    obj.typeref = (TypeRef) visit(obj.typeref, param);
    return super.visitNamedElement(obj, param);
  }

  @Override
  protected Expression visitVariable(Variable obj, T param) {
    TypeRef type = (TypeRef) visit(obj.type, param);
    assert (type != null);
    obj.type = type;
    return super.visitVariable(obj, param);
  }

  @Override
  protected Expression visitFuncReturnType(FuncReturnType obj, T param) {
    obj.type = (TypeRef) visit(obj.type, param);
    return super.visitFuncReturnType(obj, param);
  }

  @Override
  protected Expression visitCompUse(CompUse obj, T param) {
    obj.compRef = (CompRef) visit(obj.compRef, param);
    return super.visitCompUse(obj, param);
  }

  @Override
  protected Expression visitStateSimple(StateSimple obj, T param) {
    obj.entryFunc = (SimpleRef<FuncProcedure>) visit(obj.entryFunc, param);
    obj.exitFunc = (SimpleRef<FuncProcedure>) visit(obj.exitFunc, param);
    return super.visitStateSimple(obj, param);
  }

  @Override
  protected Expression visitStateComposite(StateComposite obj, T param) {
    obj.entryFunc = (SimpleRef<FuncProcedure>) visit(obj.entryFunc, param);
    obj.exitFunc = (SimpleRef<FuncProcedure>) visit(obj.exitFunc, param);
    obj.initial = (StateRef) visit(obj.initial, param);
    return super.visitStateComposite(obj, param);
  }
}