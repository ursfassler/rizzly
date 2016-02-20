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

import java.math.BigInteger;

import ast.data.reference.RefFactory;
import ast.data.reference.Reference;
import ast.data.type.Type;

public class ArrayTypeFactory {
  static public ArrayType create(BigInteger size, Type type) {
    return new ArrayType(makeName(size, type), size, createRef(type));
  }

  private static Reference createRef(Type type) {
    return RefFactory.create(type);
  }

  static public ArrayType create(int size, Type type) {
    return create(BigInteger.valueOf(size), type);
  }

  static public String makeName(BigInteger size, Type type) {
    return "Array{" + size + "," + type.getName() + "}";
  }

}
