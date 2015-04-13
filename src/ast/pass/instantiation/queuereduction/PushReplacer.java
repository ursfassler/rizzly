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

import java.util.Map;

import ast.data.expression.TupleValue;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.Reference;
import ast.data.function.Function;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.MsgPush;
import ast.data.statement.Statement;
import ast.traverser.DefTraverser;

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