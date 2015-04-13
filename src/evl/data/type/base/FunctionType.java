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

package evl.data.type.base;

import common.ElementInfo;

import evl.data.EvlList;
import evl.data.expression.reference.TypeRef;
import evl.data.type.Type;

final public class FunctionType extends Type {
  final public EvlList<TypeRef> arg;
  final public TypeRef ret;

  public FunctionType(ElementInfo info, String name, EvlList<TypeRef> arg, TypeRef ret) {
    super(info, name);
    this.arg = arg;
    this.ret = ret;
  }

  @Override
  public String toString() {
    return "func(" + arg + "):" + ret;
  }

}
