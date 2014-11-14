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

package fun.expression.reference;

import common.ElementInfo;

import fun.expression.Expression;

final public class RefIndex extends RefItem {
  private Expression index;

  public RefIndex(ElementInfo info, Expression index) {
    super(info);
    this.index = index;
  }

  public Expression getIndex() {
    return index;
  }

  public void setIndex(Expression index) {
    this.index = index;
  }

  @Override
  public String toString() {
    return "[" + index + "]";
  }

}
