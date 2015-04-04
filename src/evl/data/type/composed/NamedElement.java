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

package evl.data.type.composed;

import common.ElementInfo;

import evl.data.EvlBase;
import evl.data.Named;
import evl.data.expression.reference.SimpleRef;
import evl.data.type.Type;

final public class NamedElement extends EvlBase implements Named {
  private String name;
  final public SimpleRef<Type> ref;

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

  @Override
  public String toString() {
    return name + ref;
  }

}
