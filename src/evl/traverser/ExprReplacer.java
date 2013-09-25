package evl.traverser;

import java.util.List;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.cfg.BasicBlock;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.StringValue;
import evl.expression.binop.BinaryExp;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.unop.UnaryExp;
import evl.hfsm.Transition;
import evl.statement.bbend.CaseOptRange;
import evl.statement.bbend.CaseOptValue;
import evl.statement.bbend.ReturnExpr;
import evl.statement.normal.Assignment;
import evl.statement.normal.StoreStmt;
import evl.statement.normal.TypeCast;
import evl.statement.normal.VarDefInitStmt;
import evl.statement.phi.PhiStmt;
import evl.type.base.Range;
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

  @Override
  protected Expression visitBinaryExp(BinaryExp obj, T param) {
    obj.setLeft(visit(obj.getLeft(), param));
    obj.setRight(visit(obj.getRight(), param));
    return obj;
  }

  @Override
  protected Expression visitNumber(Number obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitUnaryExp(UnaryExp obj, T param) {
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
  protected Expression visitBoolValue(BoolValue obj, T param) {
    return obj;
  }

  @Override
  protected Expression visitTypeCast(TypeCast obj, T param) {
    obj.setValue(visit(obj.getValue(),param));
    return null;
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
  protected Expression visitRange(Range obj, T param) {
    return null;
  }

  @Override
  protected Expression visitTransition(Transition obj, T param) {
    obj.setGuard(visit(obj.getGuard(), param));
    return super.visitTransition(obj, param);
  }

  @Override
  protected Expression visitVarDefInitStmt(VarDefInitStmt obj, T param) {
    obj.setInit(visit(obj.getInit(), param));
    return super.visitVarDefInitStmt(obj, param);
  }

  @Override
  protected Expression visitPhiStmt(PhiStmt obj, T param) {
    visit(obj.getVariable(), param);
    for( BasicBlock in : obj.getInBB() ) {
      Expression expr = obj.getArg(in);
      assert(expr != null);
      expr = visit(expr, param);
      obj.addArg(in, expr);
    }
    return null;
  }

  @Override
  protected Expression visitStoreStmt(StoreStmt obj, T param) {
    obj.setExpr(visit(obj.getExpr(), param) );
    visit(obj.getAddress(), param);
    return null;
  }

}
