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

package ast.data.type.template;

import ast.data.expression.reference.Reference;
import ast.data.type.Type;

import common.ElementInfo;

public class TypeType extends Type {
  private Reference type;

  public TypeType(ElementInfo info, Reference type) {
    super(info, makeName(type));
    this.type = type;
  }

  // TODO (re)move
  @Deprecated
  public static String makeName(ast.data.expression.reference.Reference type) {
    return TypeTypeTemplate.NAME + "{" + type.toString() + "}";
  }

  public Reference getType() {
    return type;
  }

  public void setType(Reference type) {
    this.type = type;
  }

}
