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

package ast.data.statement;

import ast.data.expression.Expression;
import ast.meta.MetaList;
import ast.visitor.Visitor;

final public class CaseOptRange extends CaseOptEntry {
  public Expression start;
  public Expression end;

  public CaseOptRange(Expression start, Expression end) {
    this.start = start;
    this.end = end;
  }

  @Deprecated
  public CaseOptRange(MetaList info, Expression start, Expression end) {
    metadata().add(info);
    this.start = start;
    this.end = end;
  }

  @Override
  public String toString() {
    return start.toString() + ".." + end.toString();
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
