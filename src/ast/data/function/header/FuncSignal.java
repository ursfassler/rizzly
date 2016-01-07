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

package ast.data.function.header;

import ast.ElementInfo;
import ast.data.AstList;
import ast.data.function.InterfaceFunction;
import ast.data.function.ret.FuncReturn;
import ast.data.statement.Block;
import ast.data.variable.FuncVariable;
import ast.visitor.Visitor;

final public class FuncSignal extends InterfaceFunction {
  public FuncSignal(ElementInfo info, String name, AstList<FuncVariable> param, FuncReturn ret, Block body) {
    super(info, name, param, ret, body);
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
