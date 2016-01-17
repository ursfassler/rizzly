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
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.meta.MetaList;
import ast.specification.HasName;
import error.ErrorType;
import error.RError;

public class ChildByName {
  final private Single single;

  public ChildByName(Single single) {
    super();
    this.single = single;
  }

  public Named get(Named root, Designator path, MetaList info) {
    for (String child : path) {
      root = (Named) single.force(ChildCollector.select(root, new HasName(child)), info);
    }
    return root;
  }

  @Deprecated
  static public Ast staticGet(Ast root, Designator path, MetaList info) {
    for (String child : path) {
      root = Single.staticForce(ChildCollector.select(root, new HasName(child)), info);
    }
    return root;
  }

  static public Ast find(Ast parent, String name) {
    return Single.find(ChildCollector.select(parent, new HasName(name)));
  }

  public static Ast get(Ast parent, String name, MetaList info) {
    AstList<Ast> list = ChildCollector.select(parent, new HasName(name));
    switch (list.size()) {
      case 1:
        return list.get(0);
      case 0:
        RError.err(ErrorType.Fatal, "Name not found: " + name, info);
        return null;
      default:
        RError.err(ErrorType.Fatal, "To many items found", info);
        return null;
    }
  }

}
