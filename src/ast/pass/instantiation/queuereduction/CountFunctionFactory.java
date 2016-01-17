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

import ast.copy.Copy;
import ast.data.AstList;
import ast.data.expression.ReferenceExpression;
import ast.data.function.Function;
import ast.data.function.FunctionProperty;
import ast.data.function.header.FuncResponse;
import ast.data.function.ret.FunctionReturnType;
import ast.data.reference.RefFactory;
import ast.data.statement.Block;
import ast.data.statement.ExpressionReturn;
import ast.data.variable.FunctionVariable;
import ast.meta.MetaList;

class CountFunctionFactory {
  static public Function create(String prefix, MetaList info, QueueVariables queueVariables) {
    Block sfb = new Block();
    sfb.statements.add(new ExpressionReturn(new ReferenceExpression(RefFactory.full(info, queueVariables.getCount()))));
    Function sizefunc = new FuncResponse(prefix + "count", new AstList<FunctionVariable>(), new FunctionReturnType(info, Copy.copy(queueVariables.getCount().type)), sfb);
    sizefunc.metadata().add(info);
    sizefunc.property = FunctionProperty.Public;
    return sizefunc;
  }
}
