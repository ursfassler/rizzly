package cir.expression;

import java.math.BigInteger;

public class Number extends Expression {
  final private BigInteger value;

  public Number(BigInteger value) {
    super();
    this.value = value;
  }

  public BigInteger getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
