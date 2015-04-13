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

import java.util.ArrayList;

import error.ErrorType;
import error.RError;

public class EvlList<T extends Evl> extends ArrayList<T> {
  private static final long serialVersionUID = 2599344789209692487L;

  public EvlList() {
  }

  public EvlList(EvlList<? extends T> list) {
    super(list);
  }

  @Override
  public boolean add(T item) {
    return super.add(item);
  }

  public Named findFirst(String name) {
    for (Evl itr : this) {
      if (itr instanceof Named) {
        if (((Named) itr).name.equals(name)) {
          return (Named) itr;
        }
      }
    }
    return null;
  }

  @Deprecated
  public T find(String name) {
    EvlList<T> ret = new EvlList<T>();
    for (T itr : this) {
      if (itr instanceof Named) {
        if (((Named) itr).name.equals(name)) {
          ret.add(itr);
        }
      }
    }

    switch (ret.size()) {
      case 0:
        return null;
      case 1:
        return ret.get(0);
      default:
        for (T itr : ret) {
          RError.err(ErrorType.Hint, itr.getInfo(), "this");
        }
        RError.err(ErrorType.Fatal, "Found more than one entry with name " + name);
        return null;
    }
  }

}
