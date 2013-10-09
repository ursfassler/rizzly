package evl.expression;

import util.NumberSet;

import common.ElementInfo;

public class RangeValue extends Expression {
  final private NumberSet values;

  public RangeValue(ElementInfo info, NumberSet values) {
    super(info);
    this.values = values;
  }

  public NumberSet getValues() {
    return values;
  }

  @Override
  public String toString() {
    return values.toString();
  }

}
