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

package fun.traverser;

import java.util.List;

import error.ErrorType;
import error.RError;
import evl.data.EvlList;
import evl.data.component.composition.CompUse;
import evl.data.component.hfsm.StateComposite;
import evl.data.component.hfsm.StateSimple;
import evl.data.component.hfsm.Transition;
import evl.data.expression.Expression;
import evl.data.expression.binop.And;
import evl.data.expression.binop.ArithmeticOp;
import evl.data.expression.binop.Or;
import evl.data.expression.binop.Relation;
import evl.data.expression.reference.CompRef;
import evl.data.expression.reference.FuncRef;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefIndex;
import evl.data.expression.reference.RefTemplCall;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.expression.reference.StateRef;
import evl.data.expression.reference.TypeRef;
import evl.data.expression.unop.UnaryExp;
import evl.data.function.header.FuncProcedure;
import evl.data.function.ret.FuncReturnType;
import evl.data.statement.AssignmentMulti;
import evl.data.statement.IfOption;
import evl.data.statement.VarDefInitStmt;
import evl.data.type.composed.NamedElement;
import evl.data.variable.Variable;
import evl.traverser.DefTraverser;
import fun.other.ActualTemplateArgument;

public class ExprReplacer<T> extends DefTraverser<Expression, T> {

  @Override
  protected Expression visitExpression(Expression obj, T param) {
    Expression ret = super.visitExpression(obj, param);
    assert (ret != null);
    return ret;
  }

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
  protected Expression visitSimpleRef(SimpleRef obj, T param) {
    return obj;
  }

  @Override
  protected evl.data.expression.Expression visitReference(Reference obj, T param) {
    for (evl.data.expression.reference.RefItem item : obj.offset) {
      visit(item, param);
    }
    return obj;
  }

  @Override
  protected evl.data.expression.Expression visitRefIndex(RefIndex obj, T param) {
    obj.index = visit(obj.index, param);
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitRefCall(RefCall obj, T param) {
    visitTupleValue(obj.actualParameter, param);
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitRefName(evl.data.expression.reference.RefName obj, T param) {
    return super.visitRefName(obj, param);
  }

  @Override
  protected evl.data.expression.Expression visitRefTemplCall(RefTemplCall obj, T param) {
    EvlList<ActualTemplateArgument> list = obj.actualParameter;
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i) instanceof Expression) {
        Expression old = (Expression) list.get(i);
        Expression expr = visit(old, param);
        if (expr == null) {
          RError.err(ErrorType.Fatal, old.getInfo(), "not handled class: " + old.getClass().getCanonicalName());
        }
        list.set(i, expr);
      }
    }
    return null;
  }

  @Override
  protected Expression visitAnd(And obj, T param) {
    obj.left = visit(obj.left, param);
    obj.right = visit(obj.right, param);
    return obj;
  }

  @Override
  protected Expression visitOr(Or obj, T param) {
    obj.left = visit(obj.left, param);
    obj.right = visit(obj.right, param);
    return obj;
  }

  @Override
  protected evl.data.expression.Expression visitArithmeticOp(ArithmeticOp obj, T param) {
    obj.left = visit(obj.left, param);
    obj.right = visit(obj.right, param);
    return obj;
  }

  @Override
  protected evl.data.expression.Expression visitNumber(evl.data.expression.Number obj, T param) {
    return obj;
  }

  @Override
  protected evl.data.expression.Expression visitRelation(Relation obj, T param) {
    obj.left = visit(obj.left, param);
    obj.right = visit(obj.right, param);
    return obj;
  }

  @Override
  protected Expression visitUnaryExp(UnaryExp obj, T param) {
    obj.expr = visit(obj.expr, param);
    return obj;
  }

  @Override
  protected evl.data.expression.Expression visitStringValue(evl.data.expression.StringValue obj, T param) {
    return obj;
  }

  @Override
  protected evl.data.expression.Expression visitTupleValue(evl.data.expression.TupleValue obj, T param) {
    visitExprList(obj.value, param);
    return obj;
  }

  @Override
  protected evl.data.expression.Expression visitBoolValue(evl.data.expression.BoolValue obj, T param) {
    return obj;
  }

  @Override
  protected evl.data.expression.Expression visitAnyValue(evl.data.expression.AnyValue obj, T param) {
    return obj;
  }

  @Override
  protected evl.data.expression.Expression visitReturnExpr(evl.data.statement.ReturnExpr obj, T param) {
    obj.expr = visit(obj.expr, param);
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitCaseOptValue(evl.data.statement.CaseOptValue obj, T param) {
    obj.value = visit(obj.value, param);
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitCaseOptRange(evl.data.statement.CaseOptRange obj, T param) {
    obj.start = visit(obj.start, param);
    obj.end = visit(obj.end, param);
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitIfOption(IfOption obj, T param) {
    obj.condition = visit(obj.condition, param);
    visit(obj.code, param);
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitConstant(evl.data.variable.Constant obj, T param) {
    visit(obj.type, param);
    obj.def = visit(obj.def, param);
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitVarDefInitStmt(VarDefInitStmt obj, T param) {
    visitList(obj.variable, param);
    obj.initial = visit(obj.initial, param);
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitAssignmentMulti(AssignmentMulti obj, T param) {
    visitList(obj.left, param);
    obj.right = visit(obj.right, param);
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitCaseStmt(evl.data.statement.CaseStmt obj, T param) {
    obj.condition = visit(obj.condition, param);
    visitList(obj.option, param);
    visit(obj.otherwise, param);
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitWhileStmt(evl.data.statement.WhileStmt obj, T param) {
    obj.condition = visit(obj.condition, param);
    visit(obj.body, param);
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitTransition(Transition obj, T param) {
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
  protected evl.data.expression.Expression visitNamedElementsValue(evl.data.expression.NamedElementsValue obj, T param) {
    super.visitNamedElementsValue(obj, param);
    return obj;
  }

  @Override
  protected Expression visitVariable(Variable obj, T param) {
    TypeRef type = (TypeRef) visit(obj.type, param);
    assert (type != null);
    obj.type = type;
    return super.visitVariable(obj, param);
  }

  @Override
  protected Expression visitNamedValue(evl.data.expression.NamedValue obj, T param) {
    Expression value = visit(obj.value, param);
    assert (value != null);
    obj.value = value;
    return null;
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
    assert (obj.entryFunc != null);
    assert (obj.exitFunc != null);
    return super.visitStateSimple(obj, param);
  }

  @Override
  protected Expression visitStateComposite(StateComposite obj, T param) {
    obj.entryFunc = (SimpleRef<FuncProcedure>) visit(obj.entryFunc, param);
    obj.exitFunc = (SimpleRef<FuncProcedure>) visit(obj.exitFunc, param);
    obj.initial = (StateRef) visit(obj.initial, param);
    assert (obj.entryFunc != null);
    assert (obj.exitFunc != null);
    assert (obj.initial != null);
    return super.visitStateComposite(obj, param);
  }

}
