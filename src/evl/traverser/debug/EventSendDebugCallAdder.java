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
import java.util.List;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.expression.Number;
import evl.data.expression.TupleValue;
import evl.data.expression.reference.BaseRef;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.Reference;
import evl.data.function.Function;
import evl.data.function.header.FuncCtrlOutDataIn;
import evl.data.function.header.FuncCtrlOutDataOut;
import evl.data.function.header.FuncPrivateVoid;
import evl.data.statement.Block;
import evl.data.statement.CallStmt;
import evl.data.statement.Statement;
import evl.data.statement.intern.MsgPush;
import evl.traverser.DefTraverser;

/**
 * Inserts a message call whenever an event is sent
 *
 * @author urs
 *
 */
public class EventSendDebugCallAdder extends DefTraverser<Void, Void> {

  private StmtTraverser st;

  public EventSendDebugCallAdder(FuncPrivateVoid debugSend, ArrayList<String> names) {
    super();
    st = new StmtTraverser(debugSend, names);
  }

  public static void process(Evl obj, ArrayList<String> names, FuncPrivateVoid debugSend) {
    EventSendDebugCallAdder reduction = new EventSendDebugCallAdder(debugSend, names);
    reduction.traverse(obj, null);
  }

  @Override
  protected Void visitFunction(Function obj, Void param) {
    st.traverse(obj, null);
    return null;
  }
}

class StmtTraverser extends DefTraverser<Void, List<Statement>> {

  private FuncPrivateVoid debugSend;
  private ArrayList<String> names;
  static private ElementInfo info = ElementInfo.NO;

  public StmtTraverser(FuncPrivateVoid debugSend, ArrayList<String> names) {
    super();
    this.debugSend = debugSend;
    this.names = names;
  }

  @Override
  protected Void visitBlock(Block obj, List<Statement> param) {
    List<Statement> sl = new ArrayList<Statement>();
    for (Statement stmt : obj.statements) {
      visit(stmt, sl);
      sl.add(stmt);
    }
    obj.statements.clear();
    obj.statements.addAll(sl);
    return null;
  }

  @Override
  protected Void visitMsgPush(MsgPush obj, List<Statement> param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "Debug events not yet implemented for queued connections");
    return null;
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, List<Statement> param) {
    super.visitBaseRef(obj, param);

    boolean isOut = (obj.link instanceof FuncCtrlOutDataIn) || (obj.link instanceof FuncCtrlOutDataOut);

    if (isOut) {
      String funcName = obj.link.name;

      int numFunc = names.indexOf(funcName);
      if (numFunc >= 0) {
        param.add(makeCall(debugSend, numFunc));
      } else {
        // TODO use constants instead of strings
        assert (funcName.equals(Designator.NAME_SEP + "msgSend") || funcName.equals(Designator.NAME_SEP + "msgRecv"));
      }
    }

    return null;
  }

  private CallStmt makeCall(FuncPrivateVoid func, int numFunc) {
    // Self._sendMsg( numFunc );
    TupleValue actParam = new TupleValue(info);
    actParam.value.add(new Number(info, BigInteger.valueOf(numFunc)));

    Reference call = new Reference(info, func);
    call.offset.add(new RefCall(info, actParam));

    return new CallStmt(info, call);
  }
}
