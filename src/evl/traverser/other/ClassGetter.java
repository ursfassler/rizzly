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

package evl.traverser.other;

import java.util.Collection;

import evl.data.Evl;
import evl.data.EvlList;
import evl.traverser.DefTraverser;

public class ClassGetter<T extends Evl> extends DefTraverser<Void, Void> {
  final private EvlList<T> ret = new EvlList<T>();
  final private Class<T> kind;

  public ClassGetter(Class<T> kind) {
    super();
    this.kind = kind;
  }

  static public <T extends Evl> EvlList<T> filter(Class<T> kind, Collection<? extends Evl> list) {
    EvlList<T> ret = new EvlList<T>();
    for (Evl itr : list) {
      if (kind.isAssignableFrom(itr.getClass())) {
        ret.add((T) itr);
      }
    }
    return ret;
  }

  static public <T extends Evl> EvlList<T> getRecursive(Class<T> kind, Evl root) {
    ClassGetter<T> getter = new ClassGetter<T>(kind);
    getter.traverse(root, null);
    return getter.ret;
  }

  static public <T extends Evl> EvlList<T> getRecursive(Class<T> kind, EvlList<? extends Evl> list) {
    ClassGetter<T> getter = new ClassGetter<T>(kind);
    for (Evl itr : list) {
      getter.traverse(itr, null);
    }
    return getter.ret;
  }

  @Override
  protected Void visit(Evl obj, Void param) {
    if (kind.isAssignableFrom(obj.getClass())) {
      ret.add((T) obj);
    }
    return super.visit(obj, param);
  }

}
