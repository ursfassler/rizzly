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

import java.util.ArrayList;
import java.util.List;

import cir.expression.Expression;

public class CaseStmt extends Statement {
  private Expression condition;
  final private List<CaseEntry> entries;
  private Statement otherwise;

  public CaseStmt(Expression condition, Statement otherwise) {
    super();
    this.condition = condition;
    this.entries = new ArrayList<CaseEntry>();
    this.otherwise = otherwise;
  }

  public Expression getCondition() {
    return condition;
  }

  public List<CaseEntry> getEntries() {
    return entries;
  }

  public Statement getOtherwise() {
    return otherwise;
  }

  public void setCondition(Expression condition) {
    this.condition = condition;
  }

  public void setOtherwise(Statement otherwise) {
    this.otherwise = otherwise;
  }

  @Override
  public String toString() {
    return "switch( " + condition + " )";
  }

}
