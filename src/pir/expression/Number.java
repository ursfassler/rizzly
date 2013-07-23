package pir.expression;

public class Number extends PExpression {
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
