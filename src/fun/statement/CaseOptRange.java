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

public class CaseOptRange extends CaseOptEntry {
  private Expression start;
  private Expression end;

  public CaseOptRange(ElementInfo info, Expression start, Expression end) {
    super(info);
    this.start = start;
    this.end = end;
  }

  public Expression getStart() {
    return start;
  }

  public void setStart(Expression start) {
    this.start = start;
  }

  public Expression getEnd() {
    return end;
  }

  public void setEnd(Expression end) {
    this.end = end;
  }

  @Override
  public String toString() {
    return start.toString() + ".." + end.toString();
  }

}
