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

package fun.variable;

import common.ElementInfo;

import fun.FunBase;
import fun.expression.reference.Reference;
import fun.other.Named;

abstract public class Variable extends FunBase implements Named {
  private String name;
  private Reference type;

  public Variable(ElementInfo info, String name, Reference type) {
    super(info);
    assert (type != null);
    this.type = type;
    this.name = name;
  }

  public Reference getType() {
    return type;
  }

  public void setType(Reference type) {
    assert (type != null);
    this.type = type;
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
    return name + ":" + type.toString();
  }
}
