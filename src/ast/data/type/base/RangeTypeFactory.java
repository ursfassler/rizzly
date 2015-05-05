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

package ast.data.type.base;

import java.math.BigInteger;

import ast.ElementInfo;
import ast.data.Range;

public class RangeTypeFactory {

  static public RangeType create(Range range) {
    return new RangeType(ElementInfo.NO, makeName(range), range);
  }

  static public RangeType create(BigInteger low, BigInteger high) {
    return create(new Range(low, high));
  }

  public static RangeType create(int count) {
    BigInteger low = BigInteger.ZERO;
    BigInteger high = BigInteger.valueOf(count - 1);
    return create(low, high);
  }

  public static String makeName(Range range) {
    return "R{" + range.low + "," + range.high + "}";
  }

}
