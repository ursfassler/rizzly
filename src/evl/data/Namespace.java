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

package evl.data;

import java.util.Collection;

import common.ElementInfo;

//TODO is this class data or object?

public class Namespace extends EvlBase implements Named {
  private String name;
  final public EvlList<Evl> children = new EvlList<Evl>();

  public Namespace(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  public EvlList<Evl> getChildren() {
    return children;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public void add(Evl evl) {
    children.add(evl);
  }

  public EvlList<Namespace> getSpaces() {
    EvlList<Namespace> ret = new EvlList<Namespace>();
    for (Evl itr : children) {
      if (itr instanceof Namespace) {
        ret.add((Namespace) itr);
      }
    }
    return ret;
  }

  public EvlList<Evl> getItems() {
    EvlList<Evl> ret = new EvlList<Evl>();
    for (Evl itr : children) {
      if (!(itr instanceof Namespace)) {
        ret.add(itr);
      }
    }
    return ret;
  }

  public Namespace findSpace(String name) {
    Evl ret = children.find(name);
    if (ret instanceof Namespace) {
      return (Namespace) ret;
    } else {
      return null;
    }
  }

  public Evl findItem(String name) {
    Evl ret = children.find(name);
    if (!(ret instanceof Namespace)) {
      return ret;
    } else {
      return null;
    }
  }

  public void addAll(Collection<? extends Evl> list) {
    children.addAll(list);
  }

  @SuppressWarnings("unchecked")
  public <T extends Evl> EvlList<T> getItems(Class<T> kind, boolean recursive) {
    EvlList<T> ret = new EvlList<T>();
    for (Evl itr : getItems()) {
      if (kind.isAssignableFrom(itr.getClass())) {
        ret.add((T) itr);
      }
    }
    if (recursive) {
      for (Namespace itr : getSpaces()) {
        ret.addAll(itr.getItems(kind, true));
      }
    }
    return ret;
  }

  public void clear() {
    children.clear();
  }

  @Override
  public String toString() {
    return name;
  }

}
