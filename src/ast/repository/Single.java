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

package ast.repository;

import ast.ElementInfo;
import ast.data.Ast;
import ast.data.AstList;
import error.ErrorType;
import error.RError;

public class Single {

  static public <T extends Ast> T force(AstList<? extends T> list, ElementInfo info) {
    switch (list.size()) {
      case 1:
        return list.get(0);
      case 0:
        RError.err(ErrorType.Fatal, info, "Item not found");
        return null;
      default:
        RError.err(ErrorType.Fatal, info, "To many items found");
        return null;
    }
  }

  static public <T extends Ast> T find(AstList<? extends T> list) {
    if (list.size() == 1) {
      return list.get(0);
    } else {
      return null;
    }
  }

  static public <T extends Ast> T first(AstList<? extends T> list) {
    if (!list.isEmpty()) {
      return list.get(0);
    } else {
      return null;
    }
  }
}
