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

package ast.traverser.other;

import java.util.Collection;

import ast.data.Ast;
import ast.data.AstList;
import ast.traverser.DefTraverser;

public class ClassGetter<T extends Ast> extends DefTraverser<Void, Void> {
  final private AstList<T> ret = new AstList<T>();
  final private Class<T> kind;

  public ClassGetter(Class<T> kind) {
    super();
    this.kind = kind;
  }

  static public <T extends Ast> AstList<T> filter(Class<T> kind, Collection<? extends Ast> list) {
    AstList<T> ret = new AstList<T>();
    for (Ast itr : list) {
      if (kind.isAssignableFrom(itr.getClass())) {
        ret.add((T) itr);
      }
    }
    return ret;
  }

  static public <T extends Ast> AstList<T> getRecursive(Class<T> kind, Ast root) {
    ClassGetter<T> getter = new ClassGetter<T>(kind);
    getter.traverse(root, null);
    return getter.ret;
  }

  static public <T extends Ast> AstList<T> getRecursive(Class<T> kind, AstList<? extends Ast> list) {
    ClassGetter<T> getter = new ClassGetter<T>(kind);
    for (Ast itr : list) {
      getter.traverse(itr, null);
    }
    return getter.ret;
  }

  @Override
  protected Void visit(Ast obj, Void param) {
    if (kind.isAssignableFrom(obj.getClass())) {
      ret.add((T) obj);
    }
    return super.visit(obj, param);
  }

}
