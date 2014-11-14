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

package cir.expression;

public class UnaryOp extends Expression {
  private Op op;
  private Expression expr;

  public UnaryOp(Op op, Expression expr) {
    super();
    this.op = op;
    this.expr = expr;
  }

  public Op getOp() {
    return op;
  }

  public Expression getExpr() {
    return expr;
  }

  public void setOp(Op op) {
    this.op = op;
  }

  public void setExpr(Expression expr) {
    this.expr = expr;
  }

  @Override
  public String toString() {
    return "(" + op + " " + expr + ")";
  }

}
