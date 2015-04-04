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

package evl.pass.queue;

import common.ElementInfo;
import common.Property;

import evl.data.EvlList;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.Function;
import evl.data.function.header.FuncCtrlInDataOut;
import evl.data.function.ret.FuncReturnType;
import evl.data.statement.Block;
import evl.data.statement.ReturnExpr;
import evl.data.type.Type;
import evl.data.variable.FuncVariable;

class CountFunctionFactory {
  static public Function create(String prefix, ElementInfo info, QueueVariables queueVariables) {
    Block sfb = new Block(info);
    sfb.statements.add(new ReturnExpr(info, new Reference(info, queueVariables.getCount())));
    Function sizefunc = new FuncCtrlInDataOut(info, prefix + "count", new EvlList<FuncVariable>(), new FuncReturnType(info, new SimpleRef<Type>(ElementInfo.NO, queueVariables.getCount().type.link)), sfb);
    sizefunc.properties().put(Property.Public, true);
    return sizefunc;
  }

}
