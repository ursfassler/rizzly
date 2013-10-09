package util;

import java.math.BigInteger;

final public class Range {
  final private BigInteger low;
  final private BigInteger high;

  public Range(BigInteger low, BigInteger high) {
    assert (low.compareTo(high) <= 0);
    this.low = low;
    this.high = high;
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

  @Override
  public String toString() {
    return low + ".." + high;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((high == null) ? 0 : high.hashCode());
    result = prime * result + ((low == null) ? 0 : low.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Range other = (Range) obj;
    if (high == null) {
      if (other.high != null)
        return false;
    } else if (!high.equals(other.high))
      return false;
    if (low == null) {
      if (other.low != null)
        return false;
    } else if (!low.equals(other.low))
      return false;
    return true;
  }

}
