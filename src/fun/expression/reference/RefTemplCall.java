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

package fun.expression.reference;

import common.ElementInfo;

import fun.other.ActualTemplateArgument;
import fun.other.FunList;

final public class RefTemplCall extends RefItem {
  final private FunList<ActualTemplateArgument> actualParameter;

  public RefTemplCall(ElementInfo info, FunList<ActualTemplateArgument> expr) {
    super(info);
    this.actualParameter = expr;
  }

  public FunList<ActualTemplateArgument> getActualParameter() {
    return actualParameter;
  }

  @Override
  public String toString() {
    String ret = "";
    ret += "{";
    boolean first = true;
    for (ActualTemplateArgument gen : actualParameter) {
      if (first) {
        first = false;
      } else {
        ret += ",";
      }
      ret += gen.toString();
    }
    ret += "}";
    return ret;
  }

}
