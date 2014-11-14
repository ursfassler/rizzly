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

package fun.expression;

import common.ElementInfo;

/**
 *
 * @author urs
 */
public class ArithmeticOp extends Expression {

  private Expression left;
  private Expression right;
  private ExpOp op;

  public ArithmeticOp(ElementInfo info, Expression left, Expression right, ExpOp op) {
    super(info);
    this.left = left;
    this.right = right;
    this.op = op;
  }

  public Expression getLeft() {
    return left;
  }

  public ExpOp getOp() {
    return op;
  }

  public Expression getRight() {
    return right;
  }

  public void setLeft(Expression left) {
    this.left = left;
  }

  public void setRight(Expression right) {
    this.right = right;
  }

  @Override
  public String toString() {
    return "(" + left + " " + op + " " + right + ")";
  }

}
