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

package evl.expression.reference;

import common.ElementInfo;

import evl.expression.Expression;
import evl.other.EvlList;

final public class RefCall extends RefItem {
  private EvlList<Expression> actualParameter;

  public RefCall(ElementInfo info, EvlList<Expression> actualParameter) {
    super(info);
    this.actualParameter = new EvlList<Expression>(actualParameter);
  }

  public EvlList<Expression> getActualParameter() {
    return actualParameter;
  }

  @Override
  public String toString() {
    String ret = "";
    ret += "(";
    boolean first = true;
    for (Expression gen : actualParameter) {
      if (first) {
        first = false;
      } else {
        ret += ",";
      }
      ret += gen.toString();
    }
    ret += ")";
    return ret;
  }

}
