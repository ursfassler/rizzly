package pir.expression;

import java.math.BigInteger;

import pir.other.PirValue;

public class Number extends PExpression implements PirValue {
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
