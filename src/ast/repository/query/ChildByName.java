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

package ast.repository.query;

import ast.Designator;
import ast.ElementInfo;
import ast.data.Ast;
import ast.data.AstList;
import ast.specification.HasName;
import error.ErrorType;
import error.RError;

public class ChildByName {

  static public Ast get(Ast root, Designator path, ElementInfo info) {
    for (String child : path) {
      root = Single.force(ChildCollector.select(root, new HasName(child)), info);
    }
    return root;
  }

  static public Ast find(Ast parent, String name) {
    return Single.find(ChildCollector.select(parent, new HasName(name)));
  }

  public static Ast get(Ast parent, String name, ElementInfo info) {
    AstList<Ast> list = ChildCollector.select(parent, new HasName(name));
    switch (list.size()) {
      case 1:
        return list.get(0);
      case 0:
        RError.err(ErrorType.Fatal, info, "Name not found: " + name);
        return null;
      default:
        RError.err(ErrorType.Fatal, info, "To many items found");
        return null;
    }
  }

}
