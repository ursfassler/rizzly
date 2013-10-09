package evl.expression;

import java.math.BigInteger;

import common.ElementInfo;

public class RangeValue extends Expression {
  final private BigInteger low;
  final private BigInteger high;

  public RangeValue(ElementInfo info, BigInteger low, BigInteger high) {
    super(info);
    assert (low.compareTo(high) <= 0);
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

  @Override
  public String toString() {
    return low + ".." + high;
  }

  public static boolean doOverlap(RangeValue left, RangeValue right) {
    //TODO verify
    boolean low = left.high.compareTo(right.low) < 0;
    boolean high = left.low.compareTo(right.high) > 0;
    return !(low || high);
  }

  public static boolean isEqual(RangeValue left, RangeValue right) {
    boolean low = left.low.compareTo(right.low) == 0;
    boolean high = left.high.compareTo(right.high) == 0;
    return low && high;
  }

  public static boolean leftIsSmallerEqual(RangeValue left, RangeValue right) {
    boolean low = left.low.compareTo(right.low) >= 0;
    boolean high = left.high.compareTo(right.high) <= 0;
    return low && high;
  }

  
}
