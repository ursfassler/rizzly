package fun.type.template;

import java.math.BigInteger;

import common.ElementInfo;

import fun.type.base.BaseType;

final public class Range extends BaseType {
  final private BigInteger low;
  final private BigInteger high;

  public Range(ElementInfo info, BigInteger low, BigInteger high) {
    super(info, makeName(low, high));
    assert (low.compareTo(high) <= 0); // TODO ok?
    this.low = low;
    this.high = high;
  }

  public String makeName() {
    return makeName(low, high);
  }

  static public String makeName(BigInteger low, BigInteger high) {
    return RangeTemplate.NAME + "{" + low.toString() + "," + high.toString() + "}";
  }

  public BigInteger getLow() {
    return low;
  }

  public BigInteger getHigh() {
    return high;
  }

}
