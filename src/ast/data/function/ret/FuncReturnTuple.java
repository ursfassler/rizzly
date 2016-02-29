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

package ast.data.function.ret;

import ast.data.AstList;
import ast.data.variable.FunctionVariable;
import ast.meta.MetaList;

public class FuncReturnTuple extends FuncReturn {
  final public AstList<FunctionVariable> param;

  public FuncReturnTuple(AstList<FunctionVariable> param) {
    this.param = param;
  }

  @Deprecated
  public FuncReturnTuple(MetaList info, AstList<FunctionVariable> param) {
    metadata().add(info);
    this.param = param;
  }

}
