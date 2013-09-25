package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.binop.And;
import evl.expression.binop.Div;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.Minus;
import evl.expression.binop.Mod;
import evl.expression.binop.Mul;
import evl.expression.binop.Notequal;
import evl.expression.binop.Or;
import evl.expression.binop.Plus;
import evl.expression.binop.Shl;
import evl.expression.binop.Shr;
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
  protected Expression visitMinus(Minus obj, Void param) {
    return new Minus(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Expression visitAnd(And obj, Void param) {
    return new And(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Expression visitDiv(Div obj, Void param) {
    return new Div(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Expression visitEqual(Equal obj, Void param) {
    return new Equal(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Expression visitGreater(Greater obj, Void param) {
    return new Greater(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Expression visitLess(Less obj, Void param) {
    return new Less(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Expression visitGreaterequal(Greaterequal obj, Void param) {
    return new Greaterequal(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Expression visitLessequall(Lessequal obj, Void param) {
    return new Lessequal(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Expression visitMod(Mod obj, Void param) {
    return new Mod(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Expression visitMul(Mul obj, Void param) {
    return new Mul(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Expression visitNotequal(Notequal obj, Void param) {
    return new Notequal(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Expression visitOr(Or obj, Void param) {
    return new Or(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Expression visitPlus(Plus obj, Void param) {
    return new Plus(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Expression visitShl(Shl obj, Void param) {
    return new Shl(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Expression visitShr(Shr obj, Void param) {
    return new Shr(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }
}
