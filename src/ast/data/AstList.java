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

package ast.data;

import java.util.ArrayList;
import java.util.Collection;

public class AstList<T extends Ast> extends ArrayList<T> {
  private static final long serialVersionUID = 2599344789209692487L;

  public AstList() {
  }

  public AstList(Collection<? extends T> list) {
    super(list);
  }

  @Override
  public boolean add(T item) {
    return super.add(item);
  }

  public <C extends Ast> AstList<C> castTo(Class<C> kind) {
    return new AstList<C>((AstList<? extends C>) this);
  }

  @Override
  public String toString() {
    String ret = "";

    boolean first = true;
    for (T gen : this) {
      if (first) {
        first = false;
      } else {
        ret += ",";
      }
      ret += gen.toString();
    }
    return ret;
  }

}
