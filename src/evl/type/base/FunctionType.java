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

package evl.type.base;

import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.other.EvlList;
import evl.type.Type;

final public class FunctionType extends Type {
  private EvlList<SimpleRef<Type>> arg;
  final private SimpleRef<Type> ret;

  public FunctionType(ElementInfo info, String name, EvlList<SimpleRef<Type>> arg, SimpleRef<Type> ret) {
    super(info, name);
    this.arg = arg;
    this.ret = ret;
  }

  public EvlList<SimpleRef<Type>> getArg() {
    return arg;
  }

  public SimpleRef<Type> getRet() {
    return ret;
  }

  @Override
  public String toString() {
    return "func(" + arg + "):" + ret;
  }

}
