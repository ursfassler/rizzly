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

package evl.copy;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.function.Function;
import evl.data.function.FunctionFactory;
import evl.data.variable.FuncVariable;
import evl.traverser.NullTraverser;

public class CopyFunction extends NullTraverser<Evl, Void> {
  private CopyEvl cast;

  public CopyFunction(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Function visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Function visitFunction(Function obj, Void param) {
    EvlList<FuncVariable> arg = cast.copy(obj.param);
    Function ret = FunctionFactory.create(obj.getClass(), obj.getInfo(), obj.name, arg, cast.copy(obj.ret), cast.copy(obj.body));
    cast.getCopied().put(obj, ret);
    ret.properties().putAll(obj.properties());
    return ret;
  }

}
