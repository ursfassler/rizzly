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

package fun.type.composed;

import common.ElementInfo;

import fun.other.FunList;
import fun.type.Type;

abstract public class NamedElementType extends Type {
  final private FunList<NamedElement> element = new FunList<NamedElement>();

  public NamedElementType(ElementInfo info, String name) {
    super(info, name);
  }

  public int getSize() {
    return element.size();
  }

  public FunList<NamedElement> getElement() {
    return element;
  }

}
