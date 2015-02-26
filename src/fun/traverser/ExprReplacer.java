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
import fun.DefTraverser;
import fun.expression.AnyValue;
import fun.expression.ArithmeticOp;
import fun.expression.BoolValue;
import fun.expression.Expression;
import fun.expression.NamedElementsValue;
import fun.expression.NamedValue;
import fun.expression.Number;
import fun.expression.Relation;
import fun.expression.StringValue;
import fun.expression.TupleValue;
import fun.expression.UnaryExpression;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefName;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.hfsm.Transition;
import fun.other.ActualTemplateArgument;
import fun.other.FunList;
import fun.statement.Assignment;
import fun.statement.CaseOptRange;
import fun.statement.CaseOptValue;
import fun.statement.CaseStmt;
import fun.statement.IfOption;
import fun.statement.ReturnExpr;
import fun.statement.VarDefStmt;
import fun.statement.While;
import fun.type.composed.NamedElement;
import fun.variable.Constant;
import fun.variable.Variable;

public class ExprReplacer<T> extends DefTraverser<Expression, T> {

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
    visitTupleValue(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected Expression visitRefName(RefName obj, T param) {
    return super.visitRefName(obj, param);
  }

  @Override
  protected Expression visitRefTemplCall(RefTemplCall obj, T param) {
    FunList<ActualTemplateArgument> list = obj.getActualParameter();
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
  protected Expression visitArithmeticOp(ArithmeticOp obj, T param) {
    obj.setLeft(visit(obj.getLeft(), param));
    obj.setRight(visit(obj.getRight(), param));
    return obj;
  }

  @Override
  protected Expression visitNumber(Number obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitRelation(Relation obj, T param) {
    obj.setLeft(visit(obj.getLeft(), param));
    obj.setRight(visit(obj.getRight(), param));
    return obj;
  }

  @Override
  protected Expression visitUnaryExpression(UnaryExpression obj, T param) {
    obj.setExpr(visit(obj.getExpr(), param));
    return obj;
  }

  @Override
  protected Expression visitStringValue(StringValue obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitTupleValue(TupleValue obj, T param) {
    visitExprList(obj.getValue(), param);
    return obj;
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
  protected Expression visitIfOption(IfOption obj, T param) {
    obj.setCondition(visit(obj.getCondition(), param));
    visit(obj.getCode(), param);
    return null;
  }

  @Override
  protected Expression visitConstant(Constant obj, T param) {
    visit(obj.getType(), param);
    obj.setDef(visit(obj.getDef(), param));
    return null;
  }

  @Override
  protected Expression visitVarDefStmt(VarDefStmt obj, T param) {
    visitList(obj.getVariable(), param);
    obj.setInitial(visit(obj.getInitial(), param));
    return null;
  }

  @Override
  protected Expression visitAssignment(Assignment obj, T param) {
    visitList(obj.getLeft(), param);
    obj.setRight(visit(obj.getRight(), param));
    return null;
  }

  @Override
  protected Expression visitCaseStmt(CaseStmt obj, T param) {
    obj.setCondition(visit(obj.getCondition(), param));
    visitList(obj.getOption(), param);
    visit(obj.getOtherwise(), param);
    return null;
  }

  @Override
  protected Expression visitWhile(While obj, T param) {
    obj.setCondition(visit(obj.getCondition(), param));
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Expression visitTransition(Transition obj, T param) {
    obj.setGuard(visit(obj.getGuard(), param));
    return super.visitTransition(obj, param);
  }

  @Override
  protected Expression visitNamedElement(NamedElement obj, T param) {
    obj.setType((Reference) visit(obj.getType(), param));
    return super.visitNamedElement(obj, param);
  }

  @Override
  protected Expression visitNamedElementsValue(NamedElementsValue obj, T param) {
    super.visitNamedElementsValue(obj, param);
    return obj;
  }

  @Override
  protected Expression visitVariable(Variable obj, T param) {
    obj.setType((Reference) visit(obj.getType(), param));
    return super.visitVariable(obj, param);
  }

  @Override
  protected Expression visitNamedValue(NamedValue obj, T param) {
    obj.setValue(visit(obj.getValue(), param));
    return null;
  }

}
