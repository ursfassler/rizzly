package pir.expression;

import java.math.BigInteger;

import pir.type.TypeRef;

public class Number extends Expression {
  private BigInteger value;
  private TypeRef type;

  public Number(BigInteger value, TypeRef type) {
    super();
    this.value = value;
    this.type = type;
  }

  public BigInteger getValue() {
    return value;
  }

  public TypeRef getType() {
    return type;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }

  public void setValue(BigInteger value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
