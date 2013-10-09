package evl.type.base;

import java.math.BigInteger;
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

  public NumSet(BigInteger low, BigInteger high) {
    super(new ElementInfo(), makeName(low, high));
    this.ranges = new NumberSet(low, high);
  }

  public static String makeName(Collection<Range> ranges) {
    if (ranges.size() == 1) {
      Range r = ranges.iterator().next();
      return makeName(r.getLow(), r.getHigh());
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

  public static String makeName(BigInteger low, BigInteger high) {
    return "R{" + low + "," + high + "}";
  }

  public NumberSet getNumbers() {
    return ranges;
  }

  @Override
  public String toString() {
    return ranges.toString();
  }

}
