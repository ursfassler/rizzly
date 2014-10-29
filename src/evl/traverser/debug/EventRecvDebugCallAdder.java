package evl.traverser.debug;

import java.math.BigInteger;
import java.util.ArrayList;

import common.ElementInfo;

import evl.DefTraverser;
import evl.Evl;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.reference.RefCall;
import evl.expression.reference.Reference;
import evl.function.Function;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncPrivateVoid;
import evl.other.EvlList;
import evl.statement.CallStmt;

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
    obj.getBody().getStatements().add(0, makeCall(msgRecvFunc, numFunc));
  }

  private CallStmt makeCall(Function func, int numFunc) {
    // _sendMsg( numFunc );
    EvlList<Expression> actParam = new EvlList<Expression>();
    actParam.add(new Number(info, BigInteger.valueOf(numFunc)));

    Reference call = new Reference(info, func);
    call.getOffset().add(new RefCall(info, actParam));

    return new CallStmt(info, call);
  }
}
