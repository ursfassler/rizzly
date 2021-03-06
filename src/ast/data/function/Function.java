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

package ast.data.function;

import ast.data.AstList;
import ast.data.Named;
import ast.data.component.hfsm.StateContent;
import ast.data.function.ret.FuncReturn;
import ast.data.function.ret.FuncReturnNone;
import ast.data.statement.Block;
import ast.data.variable.FunctionVariable;
import util.Join;

abstract public class Function extends Named implements StateContent {
  final public AstList<FunctionVariable> param;
  public FuncReturn ret = new FuncReturnNone();
  public Block body = new Block();
  public FunctionProperty property = FunctionProperty.Private;

  public Function(String name, AstList<FunctionVariable> param, FuncReturn ret, Block body) {
    setName(name);
    this.param = param;
    this.ret = ret;
    this.body = body;
  }

  @Override
  public String toString() {
    String ret = getName();

    ret += "(";
    ret += Join.join(param, "; ", (p) -> p.toString());
    ret += ")";

    return ret;
  }

}
