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

package ast.traverser.debug;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ast.data.Ast;
import ast.data.expression.Number;
import ast.data.expression.TupleValue;
import ast.data.expression.reference.BaseRef;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.Reference;
import ast.data.function.Function;
import ast.data.function.header.FuncProcedure;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncSignal;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.MsgPush;
import ast.data.statement.Statement;
import ast.traverser.DefTraverser;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;

/**
 * Inserts a message call whenever an event is sent
 *
 * @author urs
 *
 */
public class EventSendDebugCallAdder extends DefTraverser<Void, Void> {

  private StmtTraverser st;

  public EventSendDebugCallAdder(FuncProcedure debugSend, ArrayList<String> names) {
    super();
    st = new StmtTraverser(debugSend, names);
  }

  public static void process(Ast obj, ArrayList<String> names, FuncProcedure debugSend) {
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

  private FuncProcedure debugSend;
  private ArrayList<String> names;
  static private ElementInfo info = ElementInfo.NO;

  public StmtTraverser(FuncProcedure debugSend, ArrayList<String> names) {
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

    boolean isOut = (obj.link instanceof FuncQuery) || (obj.link instanceof FuncSignal);

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

  private CallStmt makeCall(FuncProcedure func, int numFunc) {
    // Self._sendMsg( numFunc );
    TupleValue actParam = new TupleValue(info);
    actParam.value.add(new Number(info, BigInteger.valueOf(numFunc)));

    Reference call = new Reference(info, func);
    call.offset.add(new RefCall(info, actParam));

    return new CallStmt(info, call);
  }
}
