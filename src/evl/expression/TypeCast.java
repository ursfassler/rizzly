package evl.expression;

import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.type.Type;

public class TypeCast extends Expression {
  private Expression value;
  private SimpleRef<Type> cast;

  public TypeCast(ElementInfo info, SimpleRef<Type> cast, Expression value) {
    super(info);
    this.value = value;
    this.cast = cast;
  }

  public Expression getValue() {
    return value;
  }

  public void setValue(Expression value) {
    this.value = value;
  }

  public SimpleRef<Type> getCast() {
    return cast;
  }

  public void setCast(SimpleRef<Type> cast) {
    this.cast = cast;
  }

  @Override
  public String toString() {
    return cast + "(" + value + ")";
  }
}
