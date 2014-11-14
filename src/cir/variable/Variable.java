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

package cir.variable;

import cir.CirBase;
import cir.expression.reference.Referencable;
import cir.other.Named;
import cir.type.TypeRef;

abstract public class Variable extends CirBase implements Named, Referencable {
  private String name;
  private TypeRef type;

  public Variable(String name, TypeRef type) {
    super();
    this.name = name;
    this.type = type;
  }

  public TypeRef getType() {
    return type;
  }

  public void setType(TypeRef type) {
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

}
