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

package evl.queue;

import java.util.Map;

import evl.DefTraverser;
import evl.expression.TupleValue;
import evl.expression.reference.RefCall;
import evl.expression.reference.Reference;
import evl.function.Function;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.statement.intern.MsgPush;

class PushReplacer extends DefTraverser<Statement, Map<Function, Function>> {

  @Override
  protected Statement visitBlock(Block obj, Map<Function, Function> param) {
    for (int i = 0; i < obj.getStatements().size(); i++) {
      Statement stmt = visit(obj.getStatements().get(i), param);
      if (stmt != null) {
        obj.getStatements().set(i, stmt);
      }
    }
    return null;
  }

  @Override
  protected Statement visitMsgPush(MsgPush obj, Map<Function, Function> param) {
    assert (param.containsKey(obj.getFunc().getLink()));

    Reference call = new Reference(obj.getInfo(), param.get(obj.getFunc().getLink()));
    call.getOffset().add(new RefCall(obj.getInfo(), new TupleValue(obj.getInfo(), obj.getData())));

    return new CallStmt(obj.getInfo(), call);
  }

}
