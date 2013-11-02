package evl.expression;

import common.ElementInfo;

import evl.type.TypeRef;

public class TypeCast extends Expression {
  private Expression value;
  private TypeRef cast;

  public TypeCast(ElementInfo info, TypeRef cast, Expression value) {
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

  public TypeRef getCast() {
    return cast;
  }

  public void setCast(TypeRef cast) {
    this.cast = cast;
  }

  @Override
  public String toString() {
    return cast + "(" + value + ")";
  }
}
