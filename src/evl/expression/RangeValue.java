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

}
