package pir.expression;

import pir.other.PirValue;

public class Number extends PExpression implements PirValue {
  final private int value;

  public Number(int value) {
    super();
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }

}
