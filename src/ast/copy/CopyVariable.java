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

package ast.copy;

import ast.data.Ast;
import ast.data.variable.ConstGlobal;
import ast.data.variable.ConstPrivate;
import ast.data.variable.FuncVariable;
import ast.data.variable.StateVariable;
import ast.data.variable.Variable;
import ast.traverser.NullTraverser;

public class CopyVariable extends NullTraverser<Variable, Void> {
  private CopyAst cast;

  public CopyVariable(CopyAst cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Variable visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Variable visitFuncVariable(FuncVariable obj, Void param) {
    return new FuncVariable(obj.getInfo(), obj.name, cast.copy(obj.type));
  }

  @Override
  protected Variable visitStateVariable(StateVariable obj, Void param) {
    return new StateVariable(obj.getInfo(), obj.name, cast.copy(obj.type), cast.copy(obj.def));
  }

  @Override
  protected Variable visitConstPrivate(ConstPrivate obj, Void param) {
    return new ConstPrivate(obj.getInfo(), obj.name, cast.copy(obj.type), cast.copy(obj.def));
  }

  @Override
  protected Variable visitConstGlobal(ConstGlobal obj, Void param) {
    return new ConstGlobal(obj.getInfo(), obj.name, cast.copy(obj.type), cast.copy(obj.def));
  }

}