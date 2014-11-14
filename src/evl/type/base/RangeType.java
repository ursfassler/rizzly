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

package evl.type.base;

import util.Range;

import common.ElementInfo;

public class RangeType extends BaseType {
  final private Range range;

  public RangeType(Range range) {
    super(ElementInfo.NO, makeName(range));
    this.range = range;
  }

  public RangeType(ElementInfo info, String name, Range range) {
    super(info, name);
    this.range = range;
  }

  public static String makeName(Range range) {
    return "R{" + range.getLow() + "," + range.getHigh() + "}";
  }

  public Range getNumbers() {
    return range;
  }

}
