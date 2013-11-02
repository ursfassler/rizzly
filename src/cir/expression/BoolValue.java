package cir.expression;

public class BoolValue extends Expression {
  private boolean value;

  public BoolValue(boolean value) {
    super();
    this.value = value;
  }

  public boolean getValue() {
    return value;
  }

  public void setValue(boolean value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return Boolean.toString(value);
  }

}
