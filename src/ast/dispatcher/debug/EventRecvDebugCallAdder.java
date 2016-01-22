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

package ast.dispatcher.debug;

import java.math.BigInteger;
import java.util.ArrayList;

import ast.data.Ast;
import ast.data.expression.value.NumberValue;
import ast.data.function.Function;
import ast.data.function.header.Procedure;
import ast.data.function.header.Response;
import ast.data.function.header.Slot;
import ast.data.reference.RefFactory;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.statement.CallStmt;
import ast.dispatcher.DfsTraverser;

/**
 * Inserts a message call whenever an event is received
 *
 * @author urs
 *
 */
public class EventRecvDebugCallAdder extends DfsTraverser<Void, Void> {

  private ArrayList<String> names;
  private Procedure msgRecvFunc;

  public EventRecvDebugCallAdder(ArrayList<String> names, Procedure msgRecvFunc) {
    super();
    this.names = names;
    this.msgRecvFunc = msgRecvFunc;
  }

  public static void process(Ast obj, ArrayList<String> names, Procedure msgRecvFunc) {
    EventRecvDebugCallAdder reduction = new EventRecvDebugCallAdder(names, msgRecvFunc);
    reduction.traverse(obj, null);
  }

  @Override
  protected Void visitFuncResponse(Response obj, Void param) {
    makeDebugCall(obj);
    return null;
  }

  @Override
  protected Void visitFuncSlot(Slot obj, Void param) {
    makeDebugCall(obj);
    return null;
  }

  public void makeDebugCall(Function obj) {
    int numFunc = names.indexOf(obj.getName());
    assert (numFunc >= 0);
    obj.body.statements.add(0, makeCall(msgRecvFunc, numFunc));
  }

  private CallStmt makeCall(Function func, int numFunc) {
    // _sendMsg( numFunc );
    NumberValue arg = new NumberValue(BigInteger.valueOf(numFunc));
    LinkedReferenceWithOffset_Implementation call = RefFactory.call(func, arg);
    return new CallStmt(call);
  }
}
