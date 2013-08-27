package evl.type.base;

import java.math.BigInteger;

import common.ElementInfo;

public class Range extends BaseType {
  final private BigInteger low;
  final private BigInteger high;

  public Range(BigInteger low, BigInteger high) {
    super(new ElementInfo(), makeName(low, high));
    assert (low.compareTo(high) <= 0); // TODO ok?
    this.low = low;
    this.high = high;
  }

  public static String makeName(BigInteger low, BigInteger high) {
    return "R{" + low.toString() + "," + high.toString() + "}";
  }

  public BigInteger getLow() {
    return low;
  }

  public BigInteger getHigh() {
    return high;
  }

  static public Range narrow(Range a, Range b) {
    BigInteger low = a.getLow().max(b.getLow());
    BigInteger high = a.getHigh().min(b.getHigh());
    assert (low.compareTo(high) <= 0);
    return new Range(low, high);
  }

  public static boolean isEqual(Range left, Range right) {
    boolean low = left.low.compareTo(right.low) == 0;
    boolean high = left.high.compareTo(right.high) == 0;
    return low && high;
  }

  public static boolean leftIsSmallerEqual(Range left, Range right) {
    boolean low = left.low.compareTo(right.low) >= 0;
    boolean high = left.high.compareTo(right.high) <= 0;
    return low && high;
  }

}
