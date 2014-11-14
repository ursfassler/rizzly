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

public class UnaryExpression extends Expression {
  private Expression expr;
  private UnaryOp op;

  public UnaryExpression(ElementInfo info, Expression expr, UnaryOp op) {
    super(info);
    this.expr = expr;
    this.op = op;
  }

  public UnaryOp getOp() {
    return op;
  }

  public Expression getExpr() {
    return expr;
  }

  public void setExpr(Expression expr) {
    this.expr = expr;
  }

  public void setOp(UnaryOp op) {
    this.op = op;
  }

  @Override
  public String toString() {
    return "(" + op + expr + ")";
  }
}
