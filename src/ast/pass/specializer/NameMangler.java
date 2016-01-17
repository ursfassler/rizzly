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

package ast.pass.specializer;

import ast.data.AstList;
import ast.data.expression.value.ValueExpr;
import ast.data.template.ActualTemplateArgument;
import ast.data.type.Type;
import error.ErrorType;
import error.RError;

public class NameMangler {
  static public String name(String templateName, AstList<ActualTemplateArgument> argument) {
    String ret = templateName;
    ret += "{";
    ret += name(argument);
    ret += "}";
    return ret;
  }

  private static String name(AstList<ActualTemplateArgument> argument) {
    String ret = "";

    boolean first = true;
    for (ActualTemplateArgument arg : argument) {
      if (first) {
        first = false;
      } else {
        ret += ",";
      }
      ret += name(arg);
    }
    return ret;
  }

  private static String name(ActualTemplateArgument arg) {
    if (arg instanceof Type) {
      return ((Type) arg).getName();
    } else if (arg instanceof ValueExpr) {
      return ((ValueExpr) arg).toString();
    } else {
      RError.err(ErrorType.Fatal, "unhandled class: " + arg.getClass().getName(), arg.metadata());
      return null;
    }
  }
}
