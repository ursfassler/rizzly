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

package evl.type.special;

import common.ElementInfo;

import evl.other.EvlList;
import evl.type.Type;
import evl.type.composed.NamedElement;

public class ComponentType extends Type {
  final public EvlList<NamedElement> input = new EvlList<NamedElement>();
  final public EvlList<NamedElement> output = new EvlList<NamedElement>();

  public ComponentType(ElementInfo info, String name) {
    super(info, name);
  }

}
