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

package evl.statement;

import common.ElementInfo;

import evl.expression.Expression;
import evl.other.EvlList;

public class CaseStmt extends Statement {
  private Expression condition;
  private EvlList<CaseOpt> option;
  private Block otherwise;

  public CaseStmt(ElementInfo info, Expression condition, EvlList<CaseOpt> option, Block otherwise) {
    super(info);
    this.condition = condition;
    this.option = option;
    this.otherwise = otherwise;
  }

  public Expression getCondition() {
    return condition;
  }

  public void setCondition(Expression condition) {
    this.condition = condition;
  }

  public Block getOtherwise() {
    return otherwise;
  }

  public void setOtherwise(Block otherwise) {
    this.otherwise = otherwise;
  }

  public EvlList<CaseOpt> getOption() {
    return option;
  }

}
