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

package evl.traverser.debug;

import java.math.BigInteger;
import java.util.ArrayList;

import common.ElementInfo;

import evl.data.Evl;
import evl.data.expression.Number;
import evl.data.expression.TupleValue;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.Reference;
import evl.data.function.Function;
import evl.data.function.header.FuncCtrlInDataIn;
import evl.data.function.header.FuncCtrlInDataOut;
import evl.data.function.header.FuncPrivateVoid;
import evl.data.statement.CallStmt;
import evl.traverser.DefTraverser;

/**
 * Inserts a message call whenever an event is received
 *
 * @author urs
 *
 */
public class EventRecvDebugCallAdder extends DefTraverser<Void, Void> {

  private ArrayList<String> names;
  private FuncPrivateVoid msgRecvFunc;
  static private ElementInfo info = ElementInfo.NO;

  public EventRecvDebugCallAdder(ArrayList<String> names, FuncPrivateVoid msgRecvFunc) {
    super();
    this.names = names;
    this.msgRecvFunc = msgRecvFunc;
  }

  public static void process(Evl obj, ArrayList<String> names, FuncPrivateVoid msgRecvFunc) {
    EventRecvDebugCallAdder reduction = new EventRecvDebugCallAdder(names, msgRecvFunc);
    reduction.traverse(obj, null);
  }

  @Override
  protected Void visitFuncIfaceInRet(FuncCtrlInDataOut obj, Void param) {
    makeDebugCall(obj);
    return null;
  }

  @Override
  protected Void visitFuncIfaceInVoid(FuncCtrlInDataIn obj, Void param) {
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
    TupleValue actParam = new TupleValue(info);
    actParam.value.add(new Number(info, BigInteger.valueOf(numFunc)));

    Reference call = new Reference(info, func);
    call.offset.add(new RefCall(info, actParam));

    return new CallStmt(info, call);
  }
}
