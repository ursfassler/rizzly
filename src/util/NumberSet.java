package util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NumberSet {
  final private ArrayList<Range> ranges = new ArrayList<Range>();

  public NumberSet(Collection<Range> ranges) {
    this.ranges.addAll(ranges);
    verify();
  }

  public NumberSet(BigInteger low, BigInteger high) {
    ranges.add(new Range(low, high));
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
    //TODO check
    // and implement binary search
    for( Range r : ranges ){
      if( (r.getLow().compareTo(num) <= 0) && (r.getHigh().compareTo(num) >= 0) ){
        return true;
      }
    }
    return false;
  }

  public boolean isEmpty() {
    return ranges.isEmpty();
  }

  @Deprecated
  public BigInteger getLow() {
    assert (!isEmpty());
    return ranges.get(0).getLow();
  }

  @Deprecated
  public BigInteger getHigh() {
    assert (!isEmpty());
    return ranges.get(ranges.size() - 1).getHigh();
  }

  public static boolean isEqual(NumberSet left, NumberSet right) {
    return left.equals(right);
  }

  public static NumberSet union(NumberSet a, NumberSet b) {
    // TODO test this function
    if (a.isEmpty()) {
      return b;
    }
    if (b.isEmpty()) {
      return a;
    }
    // both types are not empty

    ArrayList<Range> all = new ArrayList<Range>();
    all.addAll(a.ranges);
    all.addAll(b.ranges);
    ArrayList<Range> ret = coalesce(all);

    return new NumberSet(ret);
  }

  /**
   * Takes a set of Ranges, merges overlapping ranges together and returns the sorted ranges.
   */
  private static ArrayList<Range> coalesce(Collection<Range> ranges) {
    ArrayList<Range> data = new ArrayList<Range>(ranges);

    if (data.size() < 2) {
      return data;
    }

    Collections.sort(data, new Comparator<Range>() {
      @Override
      public int compare(Range o1, Range o2) {
        return o1.getLow().compareTo(o2.getLow());
      }
    });

    // sorted by start number

    ArrayList<Range> ret = new ArrayList<Range>();

    Range last = data.get(0);
    for (int i = 1; i < data.size(); i++) {
      Range r = data.get(i);
      if (last.getHigh().compareTo(r.getLow()) >= 0) {
        BigInteger high = last.getHigh().max(r.getHigh());
        last = new Range(last.getLow(), high);
      } else {
        ret.add(last);
        last = r;
      }
    }
    ret.add(last);
    return ret;
  }

  public static NumberSet intersection(NumberSet a, NumberSet b) {
    List<Range> ret = new ArrayList<Range>();
    for (Range av : a.getRanges()) {
      for (Range bv : b.getRanges()) {
        Range is = intersection(av, bv);
        if (is != null) {
          ret.add(is);
        }
      }
    }
    ret = coalesce(ret);
    return new NumberSet(ret);
  }

  private static Range intersection(Range a, Range b) {
    if (b.getLow().compareTo(a.getLow()) < 0) {
      Range t = a;
      a = b;
      b = t;
    }
    assert (a.getLow().compareTo(b.getLow()) <= 0); // a.low <= b.low

    if (a.getHigh().compareTo(b.getLow()) < 0) {
      // not overlapping
      return null;
    }

    if (a.getHigh().compareTo(b.getHigh()) <= 0) {
      // overlapping
      return new Range(b.getLow(), a.getHigh());
    }

    // b is inside a
    return b;
  }

  public static NumberSet execOp(NumberSetOperator op, NumberSet left, NumberSet right) {
    List<Range> set = new ArrayList<Range>();
    for (Range lr : left.ranges) {
      for (Range rr : right.ranges) {
        set.add(op.op(lr, rr));
      }
    }
    set = coalesce(set);
    return new NumberSet(set);
  }

  /**
   * Returns a inverted type within the provided range
   */
  public static NumberSet invert(NumberSet num, BigInteger low, BigInteger high) {
    assert (low.compareTo(high) <= 0);
    assert (num.getLow().compareTo(low) >= 0);
    assert (num.getHigh().compareTo(high) <= 0);

    List<BigInteger> list = new ArrayList<BigInteger>();
    list.add(low);
    for (Range r : num.getRanges()) {
      list.add(r.getLow().subtract(BigInteger.ONE));
      list.add(r.getHigh().add(BigInteger.ONE));
    }
    list.add(high);

    int max = list.size() - 1;
    assert (max >= 3);

    if (list.get(max - 1).compareTo(list.get(max)) > 0) {
      list.remove(max);
      list.remove(max - 1);
    }

    if (list.get(0).compareTo(list.get(1)) > 0) {
      list.remove(1);
      list.remove(0);
    }

    assert (list.size() % 2 == 0);

    return makeNumSet(list);
  }

  public static NumberSet makeNumSet(List<BigInteger> list) {
    assert (list.size() % 2 == 0);
    List<Range> ranges = new ArrayList<Range>();
    for (int i = 0; i < list.size(); i += 2) {
      ranges.add(new Range(list.get(i), list.get(i + 1)));
    }
    return new NumberSet(ranges);
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

}
