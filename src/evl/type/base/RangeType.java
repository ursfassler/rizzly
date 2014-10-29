package evl.type.base;

import util.Range;

import common.ElementInfo;

public class RangeType extends BaseType {
  final private Range range;

  public RangeType(Range range) {
    super(ElementInfo.NO, makeName(range));
    this.range = range;
  }

  public RangeType(ElementInfo info, String name, Range range) {
    super(info, name);
    this.range = range;
  }

  public static String makeName(Range range) {
    return "R{" + range.getLow() + "," + range.getHigh() + "}";
  }

  public Range getNumbers() {
    return range;
  }

}
