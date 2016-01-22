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
import java.util.List;

import ast.Designator;
import ast.data.Ast;
import ast.data.expression.value.NumberValue;
import ast.data.function.Function;
import ast.data.function.header.Procedure;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.Signal;
import ast.data.reference.RefFactory;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.MsgPush;
import ast.data.statement.Statement;
import ast.dispatcher.DfsTraverser;
import error.ErrorType;
import error.RError;

/**
 * Inserts a message call whenever an event is sent
 *
 * @author urs
 *
 */
public class EventSendDebugCallAdder extends DfsTraverser<Void, Void> {

  private StmtTraverser st;

  public EventSendDebugCallAdder(Procedure debugSend, ArrayList<String> names) {
    super();
    st = new StmtTraverser(debugSend, names);
  }

  public static void process(Ast obj, ArrayList<String> names, Procedure debugSend) {
    EventSendDebugCallAdder reduction = new EventSendDebugCallAdder(debugSend, names);
    reduction.traverse(obj, null);
  }

  @Override
  protected Void visitFunction(Function obj, Void param) {
    st.traverse(obj, null);
    return null;
  }
}

class StmtTraverser extends DfsTraverser<Void, List<Statement>> {

  private Procedure debugSend;
  private ArrayList<String> names;

  public StmtTraverser(Procedure debugSend, ArrayList<String> names) {
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
    RError.err(ErrorType.Fatal, "Debug events not yet implemented for queued connections", obj.metadata());
    return null;
  }

  @Override
  protected Void visitReference(LinkedReferenceWithOffset_Implementation obj, List<Statement> param) {
    super.visitReference(obj, param);

    boolean isOut = (obj.getLink() instanceof FuncQuery) || (obj.getLink() instanceof Signal);

    if (isOut) {
      String funcName = obj.getLink().getName();

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

  private CallStmt makeCall(Procedure func, int numFunc) {
    // Self._sendMsg( numFunc );
    NumberValue arg = new NumberValue(BigInteger.valueOf(numFunc));
    LinkedReferenceWithOffset_Implementation call = RefFactory.call(func, arg);
    return new CallStmt(call);
  }
}
