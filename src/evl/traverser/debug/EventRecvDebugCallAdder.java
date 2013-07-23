package evl.traverser.debug;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import evl.DefTraverser;
import evl.Evl;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.reference.RefCall;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.function.FunctionBase;
import evl.function.impl.FuncInputHandlerEvent;
import evl.function.impl.FuncInputHandlerQuery;
import evl.function.impl.FuncPrivateVoid;
import evl.other.Named;
import evl.other.NamedList;
import evl.statement.CallStmt;
import evl.statement.Statement;


/**
 * Inserts a message call whenever an event is received
 *
 * @author urs
 *
 */
public class EventRecvDebugCallAdder extends DefTraverser<Void, Integer> {
  private ArrayList<String> names;
  private FuncPrivateVoid msgRecvFunc;
  static private ElementInfo info = new ElementInfo();

  public EventRecvDebugCallAdder(ArrayList<String> names, FuncPrivateVoid msgRecvFunc) {
    super();
    this.names = names;
    this.msgRecvFunc = msgRecvFunc;
  }

  public static void process(Evl obj, ArrayList<String> names, FuncPrivateVoid msgRecvFunc) {
    EventRecvDebugCallAdder reduction = new EventRecvDebugCallAdder(names, msgRecvFunc);
    reduction.traverse(obj, null);
  }

  // recv event

  @Override
  protected Void visitNamedList(NamedList<Named> obj, Integer param) {
    int numIface = names.indexOf(obj.getName());
    return super.visitNamedList(obj, numIface);
  }

  @Override
  protected Void visitFuncInputHandlerQuery(FuncInputHandlerQuery obj, Integer param) {
    makeDebugCall(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncInputHandlerEvent(FuncInputHandlerEvent obj, Integer param) {
    makeDebugCall(obj, param);
    return null;
  }

  public void makeDebugCall(FuncWithBody obj, Integer param) {
    assert (param != null);
    assert (param >= 0);
    int numFunc = names.indexOf(obj.getName());
    assert (numFunc >= 0);
    obj.getBody().getStatements().add(0, makeCall(msgRecvFunc, numFunc, param));
  }

  private Statement makeCall(FunctionBase func, int numFunc, int numIface) {
    // Self._sendMsg( numFunc, numIface );
    List<Expression> actParam = new ArrayList<Expression>();
    actParam.add(new Number(info, numFunc));
    actParam.add(new Number(info, numIface));

    Reference call = new Reference(info, func);
    call.getOffset().add(new RefCall(info, actParam));

    return new CallStmt(info, call);
  }

}
