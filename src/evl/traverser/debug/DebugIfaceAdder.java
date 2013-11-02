package evl.traverser.debug;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.Direction;
import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.composition.ImplComposition;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.binop.Plus;
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
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.type.TypeRef;
import evl.type.base.ArrayType;
import evl.type.base.RangeType;
import evl.variable.FuncVariable;

public class DebugIfaceAdder extends NullTraverser<Void, Void> {

  final private Interface debugIface;
  final private ArrayType arrayType;
  final private RangeType sizeType;
  final private RangeType nameNumType;
  final private ArrayList<String> names;
  final static private ElementInfo info = new ElementInfo();

  public DebugIfaceAdder(ArrayType arrayType, RangeType sizeType, RangeType nameNumType, Interface debugIface, ArrayList<String> names) {
    super();
    this.names = names;
    this.debugIface = debugIface;
    this.arrayType = arrayType;
    this.sizeType = sizeType;
    this.nameNumType = nameNumType;
  }

  public static void process(Evl obj, ArrayType arrayType, RangeType sizeType, RangeType nameNumType, Interface debugIface, ArrayList<String> names) {
    DebugIfaceAdder reduction = new DebugIfaceAdder(arrayType, sizeType, nameNumType, debugIface, names);
    reduction.traverse(obj, null);
  }

  private FuncSubHandlerEvent makeRecvProto(RangeType sizeType) {
    ListOfNamed<FuncVariable> param = new ListOfNamed<FuncVariable>();
    FuncVariable sender = new FuncVariable(info, "receiver", new TypeRef(info, arrayType));
    param.add(sender);
    FuncVariable size = new FuncVariable(info, "size", new TypeRef(info, sizeType));
    param.add(size);

    FuncSubHandlerEvent func = new FuncSubHandlerEvent(info, "msgRecv", param);
    func.setBody(new Block(info));
    return func;
  }

  private FuncSubHandlerEvent makeSendProto(RangeType sizeType) {
    ListOfNamed<FuncVariable> param = new ListOfNamed<FuncVariable>();
    FuncVariable sender = new FuncVariable(info, "sender", new TypeRef(info, arrayType));
    param.add(sender);
    FuncVariable size = new FuncVariable(info, "size", new TypeRef(info, sizeType));
    param.add(size);

    FuncSubHandlerEvent func = new FuncSubHandlerEvent(info, "msgSend", param);
    func.setBody(new Block(info));
    return func;
  }

  private FuncPrivateVoid makeDebugSend(String callname, IfaceUse debugIfaceUse) {
    Block body = new Block(info);

    FuncVariable func = new FuncVariable(info, "func", new TypeRef(info, nameNumType));
    FuncVariable iface = new FuncVariable(info, "iface", new TypeRef(info, nameNumType));

    FuncVariable path = new FuncVariable(info, "path", new TypeRef(info, arrayType));
    { // path : Array{D,N};
      VarDefStmt def = new VarDefStmt(info, path);
      body.getStatements().add(def);
    }

    { // path[0] := func;

      Reference left = new Reference(info, path, new RefIndex(info, new Number(info, BigInteger.ZERO)));
      Reference right = new Reference(info, func);
      Assignment ass = new Assignment(info, left, right);
      body.getStatements().add(ass);
    }

    { // path[1] := iface;
      Reference left = new Reference(info, path, new RefIndex(info, new Number(info, BigInteger.ONE)));
      Reference right = new Reference(info, iface);
      Assignment ass = new Assignment(info, left, right);
      body.getStatements().add(ass);
    }

    { // _debug.msgSend( path, 2 );
      List<Expression> actParam = new ArrayList<Expression>();
      actParam.add(new Reference(info, path));
      actParam.add(new Number(info, BigInteger.valueOf(2)));

      Reference call = new Reference(info, debugIfaceUse);
      call.getOffset().add(new RefName(info, callname));
      call.getOffset().add(new RefCall(info, actParam));

      body.getStatements().add(new CallStmt(info, call));
    }

    ListOfNamed<FuncVariable> param = new ListOfNamed<FuncVariable>();
    param.add(func);
    param.add(iface);
    FuncPrivateVoid rfunc = new FuncPrivateVoid(info, "_" + callname, param);
    rfunc.setBody(body);

    return rfunc;
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    // what now?
    return null;
    // throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  private List<Statement> makeCode(String callname, FuncVariable pArray, FuncVariable argSize, IfaceUse debug, String compName) {
    List<Statement> code = new ArrayList<Statement>();

    int x = names.indexOf(compName);
    assert (x >= 0);

    { // sender[size] := x;
      Reference left = new Reference(info, pArray, new RefIndex(info, new Reference(info, argSize)));
      Number right = new Number(info, BigInteger.valueOf(x));
      Assignment ass = new Assignment(info, left, right);
      code.add(ass);
    }

    FuncVariable sizeP1 = new FuncVariable(info, "sizeP1", new TypeRef(info, sizeType));

    { // sizeP1 := size + 1;
      VarDefStmt def = new VarDefStmt(info, sizeP1);
      code.add(def);

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

      for (CompUse use : obj.getComponent()) {
        Designator name = new Designator(use.getName(), "_debug");

        {
          FuncSubHandlerEvent recv = makeRecvProto(sizeType);
          List<Statement> body = makeCode(recv.getName(), recv.getParam().getList().get(0), recv.getParam().getList().get(1), debIface, use.getName());
          recv.getBody().getStatements().addAll(body);
          obj.addFunction(name.toList(), recv);
        }

        {
          FuncSubHandlerEvent send = makeSendProto(sizeType);
          List<Statement> body = makeCode(send.getName(), send.getParam().getList().get(0), send.getParam().getList().get(1), debIface, use.getName());
          send.getBody().getStatements().addAll(body);
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
