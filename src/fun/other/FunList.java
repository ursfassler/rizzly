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

package fun.other;

import java.util.ArrayList;
import java.util.List;

import fun.Fun;

public class FunList<T extends Fun> extends ArrayList<T> {

  public FunList(FunList<? extends T> list) {
    super(list);
  }

  public FunList() {
    super();
  }

  @SuppressWarnings("unchecked")
  public <R extends T> FunList<R> getItems(Class<R> kind) {
    FunList<R> ret = new FunList<R>();
    for (T itr : this) {
      if (kind.isAssignableFrom(itr.getClass())) {
        ret.add((R) itr);
      }
    }
    return ret;
  }

  public List<String> names() {
    List<String> names = new ArrayList<String>();
    for (T itr : this) {
      if (itr instanceof Named) {
        names.add(((Named) itr).getName());
      }
    }
    return names;
  }

  public Named find(String name) {
    for (T itr : this) {
      if (itr instanceof Named) {
        if (((Named) itr).getName().equals(name)) {
          return (Named) itr;
        }
      }
    }
    return null;
  }
}
