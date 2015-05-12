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
import ast.data.AstList;
import ast.data.function.Function;
import ast.data.function.FunctionFactory;
import ast.data.variable.FuncVariable;
import ast.dispatcher.NullDispatcher;

public class CopyFunction extends NullDispatcher<Ast, Void> {
  private CopyAst cast;

  public CopyFunction(CopyAst cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Function visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Function visitFunction(Function obj, Void param) {
    AstList<FuncVariable> arg = cast.copy(obj.param);
    Function ret = FunctionFactory.create(obj.getClass(), obj.getInfo(), obj.name, arg, cast.copy(obj.ret), cast.copy(obj.body));
    cast.getCopied().put(obj, ret);
    ret.property = obj.property;
    return ret;
  }

}
