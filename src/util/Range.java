package util;

import java.math.BigInteger;
import java.util.Iterator;

final public class Range implements Iterable<BigInteger> {
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

  public static boolean isIn(BigInteger num, Range range) {
    throw new RuntimeException("not yet implemented");
  }

  public Iterator<BigInteger> iterator() {
    return new RangeIterator(low, high);
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

class RangeIterator implements Iterator<BigInteger> {
  private BigInteger act;
  private BigInteger last;

  public RangeIterator(BigInteger low, BigInteger high) {
    super();
    this.act = low;
    this.last = high;
  }

  @Override
  public boolean hasNext() {
    return act.compareTo(last) <= 0;
  }

  @Override
  public BigInteger next() {
    assert (hasNext());
    BigInteger ret = act;
    act = act.add(BigInteger.ONE);
    return ret;
  }

  @Override
  public void remove() {
  }

}
