package evl.type.base;

import java.util.Collection;

import util.NumberSet;
import util.Range;

import common.ElementInfo;

public class NumSet extends BaseType {
  final private NumberSet ranges;

  public NumSet(NumberSet ranges) {
    super(new ElementInfo(), makeName(ranges.getRanges()));
    this.ranges = ranges;
  }

  public NumSet(Collection<Range> ranges) {
    super(new ElementInfo(), makeName(ranges));
    this.ranges = new NumberSet(ranges);
  }

  public NumSet(Range range) {
    super(new ElementInfo(), makeName(range));
    this.ranges = new NumberSet(range);
  }

  public static String makeName(Collection<Range> ranges) {
    if (ranges.size() == 1) {
      Range r = ranges.iterator().next();
      return makeName(r);
    }
    String ret = "NumSet{";
    boolean first = true;
    for (Range rv : ranges) {
      if (first) {
        first = false;
      } else {
        ret += ",";
      }
      ret += rv.getLow() + ".." + rv.getHigh();
    }
    ret += "}";
    return ret;
  }

  public static String makeName(Range range) {
    return "R{" + range.getLow() + "," + range.getHigh() + "}";
  }

  public NumberSet getNumbers() {
    return ranges;
  }

  @Override
  public String toString() {
    return ranges.toString();
  }

}
