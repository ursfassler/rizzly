package pir.expression;

public class BoolValue extends PExpression {
  final private boolean value;

  public BoolValue(boolean value) {
    super();
    this.value = value;
  }

  public boolean isValue() {
    return value;
  }

  @Override
  public String toString() {
    return Boolean.toString(value);
  }

}
