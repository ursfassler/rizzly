package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.ArithmeticOp;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.Relation;
import evl.expression.UnaryExpression;
import evl.expression.reference.Reference;

public class CopyExpression extends NullTraverser<Expression, Void> {
  private CopyEvl cast;

  public CopyExpression(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Expression visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    return new Reference(obj.getInfo(), obj.getLink(), cast.copy(obj.getOffset()));
  }

  @Override
  protected Expression visitBoolValue(BoolValue obj, Void param) {
    return new BoolValue(obj.getInfo(), obj.isValue());
  }

  @Override
  protected Expression visitNumber(Number obj, Void param) {
    return new Number(obj.getInfo(), obj.getValue());
  }

  @Override
  protected Expression visitArithmeticOp(ArithmeticOp obj, Void param) {
    return new ArithmeticOp(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()), obj.getOp());
  }

  @Override
  protected Expression visitRelation(Relation obj, Void param) {
    return new Relation(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()), obj.getOp());
  }

  @Override
  protected Expression visitUnaryExpression(UnaryExpression obj, Void param) {
    return new UnaryExpression(obj.getInfo(), cast.copy(obj.getExpr()), obj.getOp());
  }

}
