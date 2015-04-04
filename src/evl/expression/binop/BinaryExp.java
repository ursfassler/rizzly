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

package evl.expression.binop;

import common.ElementInfo;

import evl.expression.Expression;

/**
 *
 * @author urs
 */
abstract public class BinaryExp extends Expression {

  public Expression left;
  public Expression right;

  abstract public String getOpName();

  public BinaryExp(ElementInfo info, Expression left, Expression right) {
    super(info);
    this.left = left;
    this.right = right;
  }

  @Override
  public String toString() {
    return "(" + left + " " + getOpName() + " " + right + ")";
  }
}
