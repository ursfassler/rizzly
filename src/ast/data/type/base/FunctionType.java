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

import ast.data.AstList;
import ast.data.expression.reference.TypeRef;
import ast.data.type.Type;

import common.ElementInfo;

final public class FunctionType extends Type {
  final public AstList<TypeRef> arg;
  final public TypeRef ret;

  public FunctionType(ElementInfo info, String name, AstList<TypeRef> arg, TypeRef ret) {
    super(info, name);
    this.arg = arg;
    this.ret = ret;
  }

  @Override
  public String toString() {
    return "func(" + arg + "):" + ret;
  }

}