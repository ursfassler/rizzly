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

package evl.data.type.base;

import java.util.HashSet;
import java.util.Set;

import common.ElementInfo;

import evl.data.EvlList;
import evl.data.type.Type;

public class EnumType extends Type {
  final private EvlList<EnumElement> element = new EvlList<EnumElement>();

  public EnumType(ElementInfo info, String name) {
    super(info, name);
  }

  public EvlList<EnumElement> getElement() {
    return element;
  }

  @Deprecated
  public EnumElement find(String name) {
    return element.find(name);
  }

  public boolean isSupertypeOf(EnumType sub) {
    return element.containsAll(sub.element);
  }

  public Set<String> getNames() {
    Set<String> ret = new HashSet<String>();
    for (EnumElement elem : element) {
      ret.add(elem.name);
    }
    return ret;
  }

}
