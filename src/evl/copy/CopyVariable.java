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
import evl.variable.ConstGlobal;
import evl.variable.ConstPrivate;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

public class CopyVariable extends NullTraverser<Variable, Void> {
  private CopyEvl cast;

  public CopyVariable(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Variable visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Variable visitFuncVariable(FuncVariable obj, Void param) {
    return new FuncVariable(obj.getInfo(), obj.getName(), cast.copy(obj.getType()));
  }

  @Override
  protected Variable visitStateVariable(StateVariable obj, Void param) {
    return new StateVariable(obj.getInfo(), obj.getName(), cast.copy(obj.getType()), cast.copy(obj.getDef()));
  }

  @Override
  protected Variable visitConstPrivate(ConstPrivate obj, Void param) {
    return new ConstPrivate(obj.getInfo(), obj.getName(), cast.copy(obj.getType()), cast.copy(obj.getDef()));
  }

  @Override
  protected Variable visitConstGlobal(ConstGlobal obj, Void param) {
    return new ConstGlobal(obj.getInfo(), obj.getName(), cast.copy(obj.getType()), cast.copy(obj.getDef()));
  }

}
