package evl.expression;

import common.ElementInfo;

public class BoolValue extends Expression {
  private final boolean value;

  public BoolValue(ElementInfo info, boolean value) {
    super(info);
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
