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
