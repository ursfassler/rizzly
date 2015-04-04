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

package fun.toevl;

import evl.data.expression.Expression;
import evl.data.statement.CaseOptEntry;
import fun.Fun;
import fun.NullTraverser;
import fun.statement.CaseOptRange;
import fun.statement.CaseOptValue;

public class FunToEvlCaseOptEntry extends NullTraverser<CaseOptEntry, Void> {
  private FunToEvl fta;

  public FunToEvlCaseOptEntry(FunToEvl fta) {
    super();
    this.fta = fta;
  }

  @Override
  protected CaseOptEntry visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected CaseOptEntry visitCaseOptRange(CaseOptRange obj, Void param) {
    return new evl.data.statement.CaseOptRange(obj.getInfo(), (Expression) fta.traverse(obj.getStart(), null), (Expression) fta.traverse(obj.getEnd(), null));
  }

  @Override
  protected CaseOptEntry visitCaseOptValue(CaseOptValue obj, Void param) {
    return new evl.data.statement.CaseOptValue(obj.getInfo(), (Expression) fta.traverse(obj.getValue(), null));
  }

}
