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

package cir.statement;

import cir.expression.Expression;
import cir.expression.reference.Reference;

public class Assignment extends Statement {
  private Reference dst;
  private Expression src;

  public Assignment(Reference dst, Expression src) {
    super();
    this.dst = dst;
    this.src = src;
  }

  public Reference getDst() {
    return dst;
  }

  public Expression getSrc() {
    return src;
  }

  public void setDst(Reference dst) {
    this.dst = dst;
  }

  public void setSrc(Expression src) {
    this.src = src;
  }

  @Override
  public String toString() {
    return dst + " = " + src;
  }

}
