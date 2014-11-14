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

import evl.Evl;
import evl.NullTraverser;
import evl.function.Function;
import evl.function.FunctionFactory;
import evl.other.EvlList;
import evl.variable.FuncVariable;

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
  protected Function visitFunctionImpl(Function obj, Void param) {
    EvlList<FuncVariable> arg = cast.copy(obj.getParam());
    Function ret = FunctionFactory.create(obj.getClass(), obj.getInfo(), obj.getName(), arg, cast.copy(obj.getRet()), cast.copy(obj.getBody()));
    cast.getCopied().put(obj, ret);
    return ret;
  }

}
