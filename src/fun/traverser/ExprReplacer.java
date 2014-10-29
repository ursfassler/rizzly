package fun.traverser;

import java.util.List;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.expression.ArithmeticOp;
import fun.expression.ArrayValue;
import fun.expression.BoolValue;
import fun.expression.ExprList;
import fun.expression.Expression;
import fun.expression.NamedElementValue;
import fun.expression.Number;
import fun.expression.Relation;
import fun.expression.StringValue;
import fun.expression.UnaryExpression;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefName;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.function.FuncHeader;
import fun.hfsm.Transition;
import fun.other.ActualTemplateArgument;
import fun.other.FunList;
import fun.statement.Assignment;
import fun.statement.CaseOptRange;
import fun.statement.CaseOptValue;
import fun.statement.CaseStmt;
import fun.statement.IfOption;
import fun.statement.ReturnExpr;
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
    visitExprList(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected Expression visitRefName(RefName obj, T param) {
    return super.visitRefName(obj, param);
  }

  @Override
  protected Expression visitRefCompcall(RefTemplCall obj, T param) {
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
  protected Expression visitArrayValue(ArrayValue obj, T param) {
    visitExprList(obj.getValue(), param);
    return obj;
  }

  @Override
  protected Expression visitExprList(ExprList obj, T param) {
    visitExprList(obj.getValue(), param);
    return obj;
  }

  @Override
  protected Expression visitBoolValue(BoolValue obj, T param) {
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
  protected Expression visitAssignment(Assignment obj, T param) {
    obj.setLeft((Reference) visit(obj.getLeft(), param)); // FIXME this (cast) looks hacky
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
  protected Expression visitVariable(Variable obj, T param) {
    obj.setType((Reference) visit(obj.getType(), param));
    return super.visitVariable(obj, param);
  }

  @Override
  protected Expression visitNamedElementValue(NamedElementValue obj, T param) {
    obj.setValue(visit(obj.getValue(), param));
    return obj;
  }

  @Override
  protected Expression visitFunctionHeader(FuncHeader obj, T param) {
    obj.setRet((Reference) visit(obj.getRet(), param));
    return super.visitFunctionHeader(obj, param);
  }

}
