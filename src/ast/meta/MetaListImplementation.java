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

package ast.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class MetaListImplementation implements MetaList {
  final private ArrayList<MetaInformation> items = new ArrayList<MetaInformation>();

  public MetaListImplementation() {
  }

  public MetaListImplementation(Collection<MetaInformation> list) {
    items.addAll(list);
  }

  @Override
  public void add(MetaInformation item) {
    items.add(item);
  }

  @Override
  public void add(MetaList items) {
    for (MetaInformation itr : items) {
      add(itr);
    }
  }

  @Override
  public void clear() {
    items.clear();
  }

  @Override
  public Iterator<MetaInformation> iterator() {
    return items.iterator();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((items == null) ? 0 : items.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MetaListImplementation other = (MetaListImplementation) obj;
    if (items == null) {
      if (other.items != null)
        return false;
    } else if (!items.equals(other.items))
      return false;
    return true;
  }

}
