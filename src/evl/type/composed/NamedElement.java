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

package evl.type.composed;

import common.ElementInfo;

import evl.EvlBase;
import evl.expression.reference.SimpleRef;
import evl.other.Named;
import evl.type.Type;

final public class NamedElement extends EvlBase implements Named {
  private String name;
  final private SimpleRef<Type> ref;

  public NamedElement(ElementInfo info, String name, SimpleRef<Type> ref) {
    super(info);
    this.name = name;
    this.ref = ref;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public SimpleRef<Type> getRef() {
    return ref;
  }

  @Override
  public String toString() {
    return name + ref;
  }

}
