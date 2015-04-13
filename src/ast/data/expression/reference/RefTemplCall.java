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

package ast.data.expression.reference;

import ast.ElementInfo;
import ast.data.AstList;
import ast.data.template.ActualTemplateArgument;

final public class RefTemplCall extends RefItem {
  final public AstList<ActualTemplateArgument> actualParameter;

  public RefTemplCall(ElementInfo info, AstList<ActualTemplateArgument> expr) {
    super(info);
    this.actualParameter = expr;
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
