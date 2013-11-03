package evl.expression;

import common.ElementInfo;

public class AnyValue extends Expression {
  static final public String NAME = "?";

  public AnyValue(ElementInfo info) {
    super(info);
  }

  @Override
  public String toString() {
    return NAME;
  }

}
