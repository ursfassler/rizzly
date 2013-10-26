package evl.type.base;

import util.Range;

import common.ElementInfo;

public class RangeType extends BaseType {
  final private Range range;

  public RangeType(Range range) {
    super(new ElementInfo(), makeName(range));
    this.range = range;
  }

  public static String makeName(Range range) {
    return "R{" + range.getLow() + "," + range.getHigh() + "}";
  }

  public Range getNumbers() {
    return range;
  }

}
