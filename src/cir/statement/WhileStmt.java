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

public class WhileStmt extends Statement {
  private Expression cond;
  private Statement block;

  public WhileStmt(Expression cond, Statement block) {
    super();
    this.cond = cond;
    this.block = block;
  }

  public Expression getCondition() {
    return cond;
  }

  public Statement getBlock() {
    return block;
  }

  public Expression getCond() {
    return cond;
  }

  public void setCond(Expression cond) {
    this.cond = cond;
  }

  public void setBlock(Statement block) {
    this.block = block;
  }

  @Override
  public String toString() {
    return "while( " + cond + " )";
  }

}
