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

package ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Designator implements Iterable<String> {
  final static public String NAME_SEP = "_";
  final private ArrayList<String> name;

  public Designator() {
    this.name = new ArrayList<String>();
  }

  public Designator(List<String> name) {
    this.name = new ArrayList<String>(name);
  }

  public Designator(String name) {
    this.name = new ArrayList<String>(1);
    this.name.add(name);
  }

  public Designator(String name0, String name1) {
    this.name = new ArrayList<String>(2);
    this.name.add(name0);
    this.name.add(name1);
  }

  public Designator(Designator des, String name) {
    this.name = new ArrayList<String>(des.name);
    this.name.add(name);
  }

  public int size() {
    return name.size();
  }

  public Designator sub(int from, int to) {
    return new Designator(name.subList(from, to));
  }

  public String last() {
    return name.get(name.size() - 1);
  }

  public ArrayList<String> toList() {
    return new ArrayList<String>(name);
  }

  public String toString(String separator) {
    String res = "";
    for (int i = 0; i < name.size(); i++) {
      if (i > 0) {
        res += separator;
      }
      res += name.get(i);
    }
    return res;
  }

  @Override
  public String toString() {
    return toString(".");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    Designator other = (Designator) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  @Override
  public Iterator<String> iterator() {
    return name.iterator();
  }
}
