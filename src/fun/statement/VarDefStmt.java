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

package fun.statement;

import common.ElementInfo;

import fun.expression.Expression;
import fun.other.FunList;
import fun.variable.FuncVariable;

public class VarDefStmt extends Statement {
  final private FunList<FuncVariable> variable;
  private Expression initial;

  public VarDefStmt(ElementInfo info, FunList<FuncVariable> variable, Expression initial) {
    super(info);
    assert (initial != null);
    this.variable = variable;
    this.initial = initial;
  }

  public FunList<FuncVariable> getVariable() {
    return variable;
  }

  public Expression getInitial() {
    return initial;
  }

  public void setInitial(Expression initial) {
    assert (initial != null);
    this.initial = initial;
  }

  @Override
  public String toString() {
    return variable.toString() + " := " + initial;
  }
}
