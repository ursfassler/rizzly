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

public class IfStmt extends Statement {
  private Expression condition;
  private Statement thenBlock;
  private Statement elseBlock;

  public IfStmt(Expression condition, Statement thenBlock, Statement elseBlock) {
    super();
    this.condition = condition;
    this.thenBlock = thenBlock;
    this.elseBlock = elseBlock;
  }

  public Expression getCondition() {
    return condition;
  }

  public Statement getThenBlock() {
    return thenBlock;
  }

  public Statement getElseBlock() {
    return elseBlock;
  }

  public void setCondition(Expression condition) {
    this.condition = condition;
  }

  public void setThenBlock(Statement thenBlock) {
    this.thenBlock = thenBlock;
  }

  public void setElseBlock(Statement elseBlock) {
    this.elseBlock = elseBlock;
  }

  @Override
  public String toString() {
    return "if( " + condition + " )";
  }

}
