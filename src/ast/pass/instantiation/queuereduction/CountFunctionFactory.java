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

package ast.pass.instantiation.queuereduction;

import ast.ElementInfo;
import ast.copy.Copy;
import ast.data.AstList;
import ast.data.expression.RefExp;
import ast.data.function.Function;
import ast.data.function.FunctionProperty;
import ast.data.function.header.FuncResponse;
import ast.data.function.ret.FuncReturnType;
import ast.data.reference.RefFactory;
import ast.data.statement.Block;
import ast.data.statement.ReturnExpr;
import ast.data.variable.FuncVariable;

class CountFunctionFactory {
  static public Function create(String prefix, ElementInfo info, QueueVariables queueVariables) {
    Block sfb = new Block(info);
    sfb.statements.add(new ReturnExpr(info, new RefExp(info, RefFactory.full(info, queueVariables.getCount()))));
    Function sizefunc = new FuncResponse(info, prefix + "count", new AstList<FuncVariable>(), new FuncReturnType(info, Copy.copy(queueVariables.getCount().type)), sfb);
    sizefunc.property = FunctionProperty.Public;
    return sizefunc;
  }
}
