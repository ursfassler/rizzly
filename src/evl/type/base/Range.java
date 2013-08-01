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

}
