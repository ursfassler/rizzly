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

package evl.pass.instantiation.queuereduction;

import common.ElementInfo;

import evl.copy.Copy;
import evl.data.EvlList;
import evl.data.expression.reference.Reference;
import evl.data.function.Function;
import evl.data.function.FunctionProperty;
import evl.data.function.header.FuncResponse;
import evl.data.function.ret.FuncReturnType;
import evl.data.statement.Block;
import evl.data.statement.ReturnExpr;
import evl.data.variable.FuncVariable;

class CountFunctionFactory {
  static public Function create(String prefix, ElementInfo info, QueueVariables queueVariables) {
    Block sfb = new Block(info);
    sfb.statements.add(new ReturnExpr(info, new Reference(info, queueVariables.getCount())));
    Function sizefunc = new FuncResponse(info, prefix + "count", new EvlList<FuncVariable>(), new FuncReturnType(info, Copy.copy(queueVariables.getCount().type)), sfb);
    sizefunc.property = FunctionProperty.Public;
    return sizefunc;
  }
}
