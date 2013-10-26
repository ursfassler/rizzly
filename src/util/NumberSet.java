package util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class NumberSet implements Iterable<BigInteger> {
  final private ArrayList<Range> ranges = new ArrayList<Range>();

  public NumberSet(Collection<Range> ranges) {
    this.ranges.addAll(ranges);
    verify();
  }

  public NumberSet(Range range) {
    ranges.add(range);
    verify();
  }

  public NumberSet() {
    verify();
  }

  public ArrayList<Range> getRanges() {
    return new ArrayList<Range>(ranges);
  }

  private void verify() {
    BigInteger lastHigh = null;
    for (Range r : ranges) {
      assert (r.getLow().compareTo(r.getHigh()) <= 0);
      if (lastHigh != null) {
        assert (lastHigh.compareTo(r.getLow()) < 0);
      }
      lastHigh = r.getHigh();
    }
  }

  /**
   * Returns how many numbers the type contains.
   * 
   * @return
   */
  public BigInteger getNumberCount() {
    BigInteger num = BigInteger.ZERO;
    for (Range rv : ranges) {
      BigInteger r = rv.getHigh().subtract(rv.getLow()).add(BigInteger.ONE);
      num = num.add(r);
    }
    return num;
  }

  public boolean contains(BigInteger num) {
    // TODO check
    // and implement binary search
    for (Range r : ranges) {
      if ((r.getLow().compareTo(num) <= 0) && (r.getHigh().compareTo(num) >= 0)) {
        return true;
      }
    }
    return false;
  }

  public boolean isEmpty() {
    return ranges.isEmpty();
  }

  @Override
  public String toString() {
    return ranges.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ranges == null) ? 0 : ranges.hashCode());
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
    NumberSet other = (NumberSet) obj;
    if (ranges == null) {
      if (other.ranges != null)
        return false;
    } else if (!ranges.equals(other.ranges))
      return false;
    return true;
  }

  @Override
  public Iterator<BigInteger> iterator() {
    return new ValItr(ranges);
  }
}

class ValItr implements Iterator<BigInteger> {
  private final ArrayList<Range> ranges;
  private int idx;
  private BigInteger next;

  public ValItr(ArrayList<Range> ranges) {
    this.ranges = ranges;
    idx = 0;
    if (!ranges.isEmpty()) {
      next = ranges.get(0).getLow();
    } else {
      next = null;
    }
  }

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public BigInteger next() {
    BigInteger ret = next;
    next = next.add(BigInteger.ONE);
    Range r = ranges.get(idx);
    if (next.compareTo(r.getHigh()) > 0) {
      idx++;
      if (idx < ranges.size()) {
        next = ranges.get(idx).getLow();
      } else {
        next = null;
      }
    }
    return ret;
  }

  @Override
  public void remove() {
    throw new RuntimeException("not yet implemented");
  }

}
