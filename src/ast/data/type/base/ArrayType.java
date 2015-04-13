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

import ast.data.expression.reference.SimpleRef;
import ast.data.expression.reference.TypeRef;
import ast.data.type.Type;

import common.ElementInfo;

public class ArrayType extends BaseType {
  final public TypeRef type;
  final public BigInteger size;

  public ArrayType(BigInteger size, TypeRef type) {
    super(ElementInfo.NO, makeName(size, type));
    this.type = type;
    this.size = size;
  }

  public ArrayType(ElementInfo info, String name, BigInteger size, TypeRef type) {
    super(info, name);
    this.type = type;
    this.size = size;
  }

  // TODO extract to own/naming class
  public static String makeName(BigInteger size, TypeRef type) {
    return "Array{" + size + "," + ((SimpleRef<Type>) type).link.name + "}";
  }

}
