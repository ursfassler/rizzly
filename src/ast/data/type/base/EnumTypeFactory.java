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

package ast.data.type.base;

import java.util.Set;

import ast.data.AstList;
import ast.meta.MetaList;

public class EnumTypeFactory {

  static public EnumType create(String name, Set<String> items) {
    EnumType ret = create(name);
    for (String item : items) {
      ret.element.add(new EnumElement(item));
    }
    return ret;
  }

  @Deprecated
  static public EnumType create(MetaList info, String name) {
    EnumType enumType = create(name);
    enumType.metadata().add(info);
    return enumType;
  }

  public static EnumType create(String name) {
    return new EnumType(name, new AstList<EnumElement>());
  }

}
