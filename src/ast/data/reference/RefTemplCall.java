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

package ast.data.reference;

import ast.data.AstList;
import ast.data.template.ActualTemplateArgument;
import ast.meta.MetaList;
import ast.visitor.Visitor;

final public class RefTemplCall extends RefItem {
  final public AstList<ActualTemplateArgument> actualParameter;

  public RefTemplCall(AstList<ActualTemplateArgument> expr) {
    this.actualParameter = expr;
  }

  @Deprecated
  public RefTemplCall(MetaList info, AstList<ActualTemplateArgument> expr) {
    metadata().add(info);
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
