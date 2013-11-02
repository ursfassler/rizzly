package cir.expression;

import cir.type.TypeRef;

final public class TypeCast extends Expression {
  private Expression value;
  private TypeRef cast;

  public TypeCast(TypeRef cast, Expression value) {
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
