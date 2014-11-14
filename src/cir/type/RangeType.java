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

package cir.type;

import java.math.BigInteger;

public class RangeType extends Type {
  private final BigInteger low;
  private final BigInteger high;

  public RangeType(String name, BigInteger low, BigInteger high) {
    super(name);
    assert (low.compareTo(high) <= 0); // TODO ok?
    this.low = low;
    this.high = high;
  }

  public static String makeName(BigInteger low, BigInteger high) {
    return "R" + "{" + low.toString() + "," + high.toString() + "}";
  }

  public BigInteger getLow() {
    return low;
  }

  public BigInteger getHigh() {
    return high;
  }

  public static RangeType makeContainer(RangeType lt, RangeType rt) {
    BigInteger low = lt.getLow().min(rt.getLow());
    BigInteger high = lt.getHigh().max(rt.getHigh());
    return new RangeType(makeName(low, high), low, high);
  }

  /**
   *
   * @param lt
   * @param rt
   * @return (lt.low < rt.low) or (lt.high > rt.high)
   */
  public static boolean isBigger(RangeType lt, RangeType rt) {
    boolean lowIn = lt.getLow().compareTo(rt.getLow()) < 0; // TODO ok?
    boolean highIn = lt.getHigh().compareTo(rt.getHigh()) > 0; // TODO ok?
    return lowIn || highIn;
  }

  public static boolean isEqual(RangeType lt, RangeType rt) {
    boolean lowIn = lt.getLow().compareTo(rt.getLow()) == 0;
    boolean highIn = lt.getHigh().compareTo(rt.getHigh()) == 0;
    return lowIn && highIn;
  }
}
