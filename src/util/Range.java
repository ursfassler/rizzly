/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

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

  static public Range grow(Range a, Range b) {
    BigInteger low = a.getLow().min(b.getLow());
    BigInteger high = a.getHigh().max(b.getHigh());
    assert (low.compareTo(high) <= 0);
    return new Range(low, high);
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

  @Override
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
