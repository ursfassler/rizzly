package evl.traverser.debug;

import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.Direction;
import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.cfg.ReturnVoid;
import evl.composition.ImplComposition;
import evl.expression.ArithmeticOp;
import evl.expression.ExpOp;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.impl.FuncPrivateVoid;
import evl.function.impl.FuncSubHandlerEvent;
import evl.other.CompUse;
import evl.other.IfaceUse;
import evl.other.ImplElementary;
import evl.other.Interface;
import evl.other.ListOfNamed;
import evl.other.Namespace;
import evl.statement.Assignment;
import evl.statement.CallStmt;
import evl.statement.VarDefStmt;
import evl.type.base.ArrayType;
import evl.type.base.Range;
import evl.variable.FuncVariable;

public class DebugIfaceAdder extends NullTraverser<Void, Void> {
  private Interface debugIface;
  private ArrayType arrayType;
  private Range sizeType;
  private ArrayList<String> names;
  static private ElementInfo info = new ElementInfo();

  public DebugIfaceAdder(ArrayType arrayType, Range sizeType, Interface debugIface, ArrayList<String> names) {
    super();
    this.names = names;
    this.debugIface = debugIface;
    this.arrayType = arrayType;
    this.sizeType = sizeType;
  }

  public static void process(Evl obj, ArrayType arrayType, Range sizeType, Interface debugIface, ArrayList<String> names) {
    DebugIfaceAdder reduction = new DebugIfaceAdder(arrayType, sizeType, debugIface, names);
    reduction.traverse(obj, null);
  }

//  public FuncSubHandlerEvent makeRecvProto(ArrayType arrayType, Range sizeType) {
//    ListOfNamed<FuncVariable> param = new ListOfNamed<FuncVariable>();
//    FuncVariable sender = new FuncVariable(info, "receiver", arrayType);
//    param.add(sender);
//    FuncVariable size = new FuncVariable(info, "size", sizeType);
//    param.add(size);
//
//    FuncSubHandlerEvent func = new FuncSubHandlerEvent(info, "msgRecv", param);
//    func.setBody(new BasicBlockList(info));
//    return func;
//  }
//
//  public FuncSubHandlerEvent makeSendProto(ArrayType arrayType, Range sizeType) {
//    ListOfNamed<FuncVariable> param = new ListOfNamed<FuncVariable>();
//    FuncVariable sender = new FuncVariable(info, "sender", arrayType);
//    param.add(sender);
//    FuncVariable size = new FuncVariable(info, "size", sizeType);
//    param.add(size);
//
//    FuncSubHandlerEvent func = new FuncSubHandlerEvent(info, "msgSend", param);
//    func.setBody(new BasicBlockList(info));
//    return func;
//  }
//
//  private FuncPrivateVoid makeDebugSend(String callname, ArrayType arrayType, Range sizeType, IfaceUse debugIfaceUse) {
//    BasicBlock body = new BasicBlock(info, "bodyBB");
//
//    FuncVariable func = new FuncVariable(info, "func", sizeType);
//    FuncVariable iface = new FuncVariable(info, "iface", sizeType);
//
//    FuncVariable path;
//    { // path : Array{D,N};
//      path = new FuncVariable(info, "path", arrayType);
//      VarDefStmt def = new VarDefStmt(info, path);
//      body.getCode().add(def);
//    }
//
//    { // path[0] := func;
//      Reference arridx = new Reference(info, path);
//      arridx.getOffset().add(new RefIndex(info, new Number(info, 0)));
//      Assignment ass = new Assignment(info, arridx, new Reference(info, func));
//      body.getCode().add(ass);
//    }
//
//    { // path[1] := iface;
//      Reference arridx = new Reference(info, path);
//      arridx.getOffset().add(new RefIndex(info, new Number(info, 1)));
//      Assignment ass = new Assignment(info, arridx, new Reference(info, iface));
//      body.getCode().add(ass);
//    }
//
//    { // _debug.msgSend( path, 2 );
//      List<Expression> actParam = new ArrayList<Expression>();
//      actParam.add(new Reference(info, path));
//      actParam.add(new Number(info, 2));
//
//      Reference call = new Reference(info, debugIfaceUse);
//      call.getOffset().add(new RefName(info, callname));
//      call.getOffset().add(new RefCall(info, actParam));
//
//      body.getCode().add(new CallStmt(info, call));
//    }
//    body.setEnd(new ReturnVoid(info));
//
//    ListOfNamed<FuncVariable> param = new ListOfNamed<FuncVariable>();
//    param.add(func);
//    param.add(iface);
//
//    BasicBlockList bblist = new BasicBlockList(info);
//    bblist.getBasicBlocksOld().add(body);
//    FuncPrivateVoid rfunc = new FuncPrivateVoid(info, "_" + callname, param);
//    rfunc.setBody(bblist);
//
//    return rfunc;
//  }
//
  @Override
  protected Void visitDefault(Evl obj, Void param) {
    //return null;
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

//  @Override
//  protected Void visitNamespace(Namespace obj, Void param) {
//    visitItr(obj, param);
//    return null;
//  }
//
//  private BasicBlockList makeCode(String callname, FuncVariable varArray, FuncVariable varSize, IfaceUse debug, String compName) {
//    BasicBlock bb = new BasicBlock(info, "bodyBB");
//
//    int x = names.indexOf(compName);
//    assert (x >= 0);
//
//    { // sender[size] := x;
//      Reference arridx = new Reference(info, varArray);
//      arridx.getOffset().add(new RefIndex(info, new Reference(info, varSize)));
//      Assignment ass = new Assignment(info, arridx, new Number(info, x));
//      bb.getCode().add(ass);
//    }
//
//    { // size := size + 1;
//      Expression expr = new ArithmeticOp(info, new Reference(info, varSize), new Number(info, 1), ExpOp.PLUS);
//      Assignment ass = new Assignment(info, new Reference(info, varSize), expr);
//      bb.getCode().add(ass);
//    }
//
//    { // Self._debug.sendMsg( sender, size );
//      List<Expression> actParam = new ArrayList<Expression>();
//      actParam.add(new Reference(info, varArray));
//      actParam.add(new Reference(info, varSize));
//
//      Reference call = new Reference(info, debug);
//      call.getOffset().add(new RefName(info, callname));
//      call.getOffset().add(new RefCall(info, actParam));
//
//      bb.getCode().add(new CallStmt(info, call));
//    }
//
//    bb.setEnd(new ReturnVoid(info));
//
//    BasicBlockList bblist = new BasicBlockList(info);
//    bblist.getBasicBlocksOld().add(bb);
//
//    return bblist;
//  }
//
//  @Override
//  protected Void visitImplElementary(ImplElementary obj, Void param) {
//    IfaceUse debIface;
//    { // add iface
//      debIface = new IfaceUse(info, "_debug", debugIface);
//      obj.getIface(Direction.out).add(debIface);
//    }
//
//    FuncPrivateVoid debugSend = makeDebugSend("msgSend", arrayType, sizeType, debIface);
//    FuncPrivateVoid debugRecv = makeDebugSend("msgRecv", arrayType, sizeType, debIface);
//    obj.getInternalFunction().add(debugSend);
//    obj.getInternalFunction().add(debugRecv);
//
//    EventSendDebugCallAdder.process(obj, names, debugSend);
//    EventRecvDebugCallAdder.process(obj, names, debugRecv);
//
//    {// add callback
//
//      for (CompUse use : obj.getComponent()) {
//        Designator name = new Designator(use.getName(), "_debug");
//
//        {
//          FuncSubHandlerEvent recv = makeRecvProto(arrayType, sizeType);
//          recv.setBody(makeCode(recv.getName(), recv.getParam().getList().get(0), recv.getParam().getList().get(1), debIface, use.getName()));
//          obj.addFunction(name.toList(), recv);
//        }
//
//        {
//          FuncSubHandlerEvent send = makeSendProto(arrayType, sizeType);
//          send.setBody(makeCode(send.getName(), send.getParam().getList().get(0), send.getParam().getList().get(1), debIface, use.getName()));
//          obj.addFunction(name.toList(), send);
//        }
//      }
//    }
//
//    return null;
//  }
//
//  @Override
//  protected Void visitImplComposition(ImplComposition obj, Void param) {
//    throw new RuntimeException("not yet implemented");
//  }
//
}
