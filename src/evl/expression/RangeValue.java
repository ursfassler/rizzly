package evl.expression;

import util.Range;

import common.ElementInfo;

public class RangeValue extends Expression {
  final private Range values;

  public RangeValue(ElementInfo info, Range values) {
    super(info);
    this.values = values;
  }

  public Range getValues() {
    return values;
  }

  @Override
  public String toString() {
    return values.toString();
  }

}
