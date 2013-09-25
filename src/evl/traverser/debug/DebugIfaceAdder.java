package evl.traverser.debug;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.Direction;
import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlockList;
import evl.composition.ImplComposition;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.binop.Plus;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.RefPtrDeref;
import evl.expression.reference.Reference;
import evl.function.impl.FuncPrivateVoid;
import evl.function.impl.FuncSubHandlerEvent;
import evl.other.CompUse;
import evl.other.IfaceUse;
import evl.other.ImplElementary;
import evl.other.Interface;
import evl.other.ListOfNamed;
import evl.other.Namespace;
import evl.statement.normal.Assignment;
import evl.statement.normal.CallStmt;
import evl.statement.normal.GetElementPtr;
import evl.statement.normal.NormalStmt;
import evl.statement.normal.StackMemoryAlloc;
import evl.statement.normal.StoreStmt;
import evl.type.TypeRef;
import evl.type.base.Range;
import evl.type.special.PointerType;
import evl.variable.SsaVariable;
import evl.variable.Variable;

public class DebugIfaceAdder extends NullTraverser<Void, Void> {

  final private Interface debugIface;
  final private PointerType pArrayType;
  final private PointerType pArrayElemType;
  final private Range sizeType;
  final private Range nameNumType;
  final private ArrayList<String> names;
  final static private ElementInfo info = new ElementInfo();

  public DebugIfaceAdder(PointerType pArrayType, PointerType pArrayElemType, Range sizeType, Range nameNumType, Interface debugIface, ArrayList<String> names) {
    super();
    this.names = names;
    this.debugIface = debugIface;
    this.pArrayType = pArrayType;
    this.sizeType = sizeType;
    this.nameNumType = nameNumType;
    this.pArrayElemType = pArrayElemType;
  }

  public static void process(Evl obj, PointerType pArrayType, PointerType pArrayElemType, Range sizeType, Range nameNumType, Interface debugIface, ArrayList<String> names) {
    DebugIfaceAdder reduction = new DebugIfaceAdder(pArrayType, pArrayElemType, sizeType, nameNumType, debugIface, names);
    reduction.traverse(obj, null);
  }

  private FuncSubHandlerEvent makeRecvProto(Range sizeType) {
    ListOfNamed<Variable> param = new ListOfNamed<Variable>();
    SsaVariable sender = new SsaVariable(info, "receiver", new TypeRef(info, pArrayType));
    param.add(sender);
    SsaVariable size = new SsaVariable(info, "size", new TypeRef(info, sizeType));
    param.add(size);

    FuncSubHandlerEvent func = new FuncSubHandlerEvent(info, "msgRecv", param);
    func.setBody(new BasicBlockList(info));
    return func;
  }

  private FuncSubHandlerEvent makeSendProto(Range sizeType) {
    ListOfNamed<Variable> param = new ListOfNamed<Variable>();
    SsaVariable sender = new SsaVariable(info, "sender", new TypeRef(info, pArrayType));
    param.add(sender);
    SsaVariable size = new SsaVariable(info, "size", new TypeRef(info, sizeType));
    param.add(size);

    FuncSubHandlerEvent func = new FuncSubHandlerEvent(info, "msgSend", param);
    func.setBody(new BasicBlockList(info));
    return func;
  }

  private FuncPrivateVoid makeDebugSend(String callname, IfaceUse debugIfaceUse) {
    ArrayList<NormalStmt> code = new ArrayList<NormalStmt>();

    SsaVariable func = new SsaVariable(info, "func", new TypeRef(info, nameNumType));
    SsaVariable iface = new SsaVariable(info, "iface", new TypeRef(info, nameNumType));

    SsaVariable path = new SsaVariable(info, "path", new TypeRef(info, pArrayType));
    { // path : Array{D,N};
      StackMemoryAlloc def = new StackMemoryAlloc(info, path);
      code.add(def);
    }

    { // path[0] := func;
      SsaVariable pidx0 = new SsaVariable(info, "pidx0", new TypeRef(info, pArrayElemType));
      Reference arridx = new Reference(info, path);
      arridx.getOffset().add(new RefPtrDeref(info));
      arridx.getOffset().add(new RefIndex(info, new Number(info, BigInteger.ZERO)));
      GetElementPtr gep = new GetElementPtr(info, pidx0, arridx);
      StoreStmt store = new StoreStmt(info, new Reference(info, pidx0), new Reference(info, func));

      code.add(gep);
      code.add(store);
    }

    { // path[1] := iface;
      SsaVariable pidx1 = new SsaVariable(info, "pidx1", new TypeRef(info, pArrayElemType));
      Reference arridx = new Reference(info, path);
      arridx.getOffset().add(new RefPtrDeref(info));
      arridx.getOffset().add(new RefIndex(info, new Number(info, BigInteger.ONE)));
      GetElementPtr gep = new GetElementPtr(info, pidx1, arridx);
      StoreStmt store = new StoreStmt(info, new Reference(info, pidx1), new Reference(info, iface));

      code.add(gep);
      code.add(store);
    }

    { // _debug.msgSend( path, 2 );
      List<Expression> actParam = new ArrayList<Expression>();
      actParam.add(new Reference(info, path));
      actParam.add(new Number(info, BigInteger.valueOf(2)));

      Reference call = new Reference(info, debugIfaceUse);
      call.getOffset().add(new RefName(info, callname));
      call.getOffset().add(new RefCall(info, actParam));

      code.add(new CallStmt(info, call));
    }

    BasicBlockList bblist = new BasicBlockList(info);
    bblist.insertCodeAfterEntry(code, "body");

    ListOfNamed<Variable> param = new ListOfNamed<Variable>();
    param.add(func);
    param.add(iface);
    FuncPrivateVoid rfunc = new FuncPrivateVoid(info, "_" + callname, param);
    rfunc.setBody(bblist);

    return rfunc;
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    //what now?
    return null;
//    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  private List<NormalStmt> makeCode(String callname, SsaVariable pArray, SsaVariable argSize, IfaceUse debug, String compName) {
    List<NormalStmt> code = new ArrayList<NormalStmt>();

    int x = names.indexOf(compName);
    assert ( x >= 0 );

    { // sender[size] := x;
      SsaVariable pSendSize = new SsaVariable(info, "pSendSize", new TypeRef(info, pArrayElemType));
      Reference ref = new Reference(info, pArray);
      ref.getOffset().add(new RefPtrDeref(info));
      ref.getOffset().add(new RefIndex(info, new Reference(info, argSize)));
      GetElementPtr gep = new GetElementPtr(info, pSendSize, ref);
      StoreStmt store = new StoreStmt(info, new Reference(info, pSendSize), new Number(info, BigInteger.valueOf(x)));

      code.add(gep);
      code.add(store);
    }

    SsaVariable sizeP1 = new SsaVariable(info, "sizeP1", new TypeRef(info, sizeType));

    { // sizeP1 := size + 1;
      Expression expr = new Plus(info, new Reference(info, argSize), new Number(info, BigInteger.ONE));
      Assignment ass = new Assignment(info, new Reference(info, sizeP1), expr);
      code.add(ass);
    }

    { // Self._debug.sendMsg( sender, sizeP1 );
      List<Expression> actParam = new ArrayList<Expression>();
      actParam.add(new Reference(info, pArray));
      actParam.add(new Reference(info, sizeP1));

      Reference call = new Reference(info, debug);
      call.getOffset().add(new RefName(info, callname));
      call.getOffset().add(new RefCall(info, actParam));

      code.add(new CallStmt(info, call));
    }

    return code;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    IfaceUse debIface;
    { // add iface
      debIface = new IfaceUse(info, "_debug", debugIface);
      obj.getIface(Direction.out).add(debIface);
    }

    FuncPrivateVoid debugSend = makeDebugSend("msgSend", debIface);
    FuncPrivateVoid debugRecv = makeDebugSend("msgRecv", debIface);
    obj.getInternalFunction().add(debugSend);
    obj.getInternalFunction().add(debugRecv);

    EventSendDebugCallAdder.process(obj, names, debugSend);
    EventRecvDebugCallAdder.process(obj, names, debugRecv);

    {// add callback

      for( CompUse use : obj.getComponent() ) {
        Designator name = new Designator(use.getName(), "_debug");

        {
          FuncSubHandlerEvent recv = makeRecvProto(sizeType);
          List<NormalStmt> body = makeCode(recv.getName(), (SsaVariable) recv.getParam().getList().get(0), (SsaVariable) recv.getParam().getList().get(1), debIface, use.getName());
          recv.getBody().insertCodeAfterEntry(body, "body");
          obj.addFunction(name.toList(), recv);
        }

        {
          FuncSubHandlerEvent send = makeSendProto(sizeType);
          List<NormalStmt> body = makeCode(send.getName(), (SsaVariable) send.getParam().getList().get(0), (SsaVariable) send.getParam().getList().get(1), debIface, use.getName());
          send.getBody().insertCodeAfterEntry(body, "body");
          obj.addFunction(name.toList(), send);
        }
      }
    }

    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }
}
