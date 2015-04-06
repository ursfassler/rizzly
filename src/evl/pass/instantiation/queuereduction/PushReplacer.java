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

import java.util.Map;

import evl.data.expression.TupleValue;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.Reference;
import evl.data.function.Function;
import evl.data.statement.Block;
import evl.data.statement.CallStmt;
import evl.data.statement.Statement;
import evl.data.statement.intern.MsgPush;
import evl.traverser.DefTraverser;

class PushReplacer extends DefTraverser<Statement, Map<Function, Function>> {

  @Override
  protected Statement visitBlock(Block obj, Map<Function, Function> param) {
    for (int i = 0; i < obj.statements.size(); i++) {
      Statement stmt = visit(obj.statements.get(i), param);
      if (stmt != null) {
        obj.statements.set(i, stmt);
      }
    }
    return null;
  }

  @Override
  protected Statement visitMsgPush(MsgPush obj, Map<Function, Function> param) {
    assert (param.containsKey(obj.func.link));

    Reference call = new Reference(obj.getInfo(), param.get(obj.func.link));
    call.offset.add(new RefCall(obj.getInfo(), new TupleValue(obj.getInfo(), obj.data)));

    return new CallStmt(obj.getInfo(), call);
  }

}
