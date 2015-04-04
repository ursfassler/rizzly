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

package evl.pass.debug;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.ElementInfo;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.component.composition.CompUse;
import evl.data.component.composition.ImplComposition;
import evl.data.component.elementary.ImplElementary;
import evl.data.expression.Expression;
import evl.data.expression.Number;
import evl.data.expression.TupleValue;
import evl.data.expression.TypeCast;
import evl.data.expression.binop.Plus;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefIndex;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.header.FuncCtrlOutDataOut;
import evl.data.function.header.FuncPrivateVoid;
import evl.data.function.header.FuncSubHandlerEvent;
import evl.data.function.ret.FuncReturnNone;
import evl.data.statement.Assignment;
import evl.data.statement.AssignmentSingle;
import evl.data.statement.Block;
import evl.data.statement.CallStmt;
import evl.data.statement.Statement;
import evl.data.statement.VarDefStmt;
import evl.data.type.Type;
import evl.data.type.base.ArrayType;
import evl.data.type.base.RangeType;
import evl.data.type.special.VoidType;
import evl.data.variable.FuncVariable;
import evl.traverser.NullTraverser;
import evl.traverser.debug.EventRecvDebugCallAdder;
import evl.traverser.debug.EventSendDebugCallAdder;

public class DebugIfaceAdder extends NullTraverser<Void, Void> {
  final private ArrayType arrayType;
  final private RangeType sizeType;
  final private RangeType nameNumType;
  final private VoidType voidType;
  final private ArrayList<String> names;
  final static private ElementInfo info = ElementInfo.NO;

  public DebugIfaceAdder(ArrayType arrayType, RangeType sizeType, RangeType nameNumType, VoidType voidType, ArrayList<String> names) {
    super();
    this.names = names;
    this.arrayType = arrayType;
    this.sizeType = sizeType;
    this.nameNumType = nameNumType;
    this.voidType = voidType;
  }

  private FuncSubHandlerEvent makeRecvProto(RangeType sizeType) {
    EvlList<FuncVariable> param = new EvlList<FuncVariable>();
    FuncVariable sender = new FuncVariable(info, "receiver", tr(arrayType));
    param.add(sender);
    FuncVariable size = new FuncVariable(info, "size", tr(sizeType));
    param.add(size);

    FuncSubHandlerEvent func = new FuncSubHandlerEvent(info, Designator.NAME_SEP + "msgRecv", param, new FuncReturnNone(info), new Block(info));
    func.body = new Block(info);
    return func;
  }

  private SimpleRef<Type> tr(Type type) {
    return new SimpleRef<Type>(info, type);
  }

  private FuncSubHandlerEvent makeSendProto(RangeType sizeType) {
    EvlList<FuncVariable> param = new EvlList<FuncVariable>();
    FuncVariable sender = new FuncVariable(info, "sender", new SimpleRef<Type>(info, arrayType));
    param.add(sender);
    FuncVariable size = new FuncVariable(info, "size", new SimpleRef<Type>(info, sizeType));
    param.add(size);

    FuncSubHandlerEvent func = new FuncSubHandlerEvent(info, Designator.NAME_SEP + "msgSend", param, new FuncReturnNone(info), new Block(info));
    return func;
  }

  private FuncPrivateVoid makeDebugSend(String callname, FuncCtrlOutDataOut sendProto) {
    Block body = new Block(info);

    FuncVariable func = new FuncVariable(info, "func", new SimpleRef<Type>(info, nameNumType));

    FuncVariable path = new FuncVariable(info, "path", new SimpleRef<Type>(info, arrayType));
    { // path : Array{D,N};
      VarDefStmt def = new VarDefStmt(info, path);
      body.statements.add(def);
    }

    { // path[0] := func;

      Reference left = new Reference(info, path, new RefIndex(info, new Number(info, BigInteger.ZERO)));
      Reference right = new Reference(info, func);
      Assignment ass = new AssignmentSingle(info, left, right);
      body.statements.add(ass);
    }

    { // _debug.msgSend( path, 1 );
      TupleValue actParam = new TupleValue(info);
      actParam.value.add(new Reference(info, path));
      actParam.value.add(new Number(info, BigInteger.valueOf(1)));

      Reference call = new Reference(info, sendProto);
      call.offset.add(new RefCall(info, actParam));

      body.statements.add(new CallStmt(info, call));
    }

    EvlList<FuncVariable> param = new EvlList<FuncVariable>();
    param.add(func);
    FuncPrivateVoid rfunc = new FuncPrivateVoid(info, "_" + callname, param, new FuncReturnNone(info), body);

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
    visitList(obj.getChildren(), param);
    return null;
  }

  private List<Statement> makeCode(String callname, FuncVariable pArray, FuncVariable argSize, FuncCtrlOutDataOut proto, String compName) {
    EvlList<Statement> code = new EvlList<Statement>();

    int x = names.indexOf(compName);
    assert (x >= 0);

    { // sender[size] := x;
      Reference left = new Reference(info, pArray, new RefIndex(info, new Reference(info, argSize)));
      Number right = new Number(info, BigInteger.valueOf(x));
      Assignment ass = new AssignmentSingle(info, left, right);
      code.add(ass);
    }

    FuncVariable sizeP1 = new FuncVariable(info, "sizeP1", new SimpleRef<Type>(info, sizeType));

    { // sizeP1 := size + 1;
      VarDefStmt def = new VarDefStmt(info, sizeP1);
      code.add(def);

      Expression expr = new Plus(info, new Reference(info, argSize), new Number(info, BigInteger.ONE));
      expr = new TypeCast(info, new SimpleRef<Type>(info, sizeType), expr);
      Assignment ass = new AssignmentSingle(info, new Reference(info, sizeP1), expr);
      code.add(ass);
    }

    { // Self._debug.sendMsg( sender, sizeP1 );
      TupleValue actParam = new TupleValue(info);
      actParam.value.add(new Reference(info, pArray));
      actParam.value.add(new Reference(info, sizeP1));

      Reference call = new Reference(info, proto);
      call.offset.add(new RefCall(info, actParam));

      code.add(new CallStmt(info, call));
    }

    return code;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {

    FuncCtrlOutDataOut sendProto = makeMsg(Designator.NAME_SEP + "msgSend", "sender");
    obj.iface.add(sendProto);
    FuncCtrlOutDataOut recvProto = makeMsg(Designator.NAME_SEP + "msgRecv", "receiver");
    obj.iface.add(recvProto);

    FuncPrivateVoid debugSend = makeDebugSend("iMsgSend", sendProto);
    FuncPrivateVoid debugRecv = makeDebugSend("iMsgRecv", recvProto);
    obj.function.add(debugSend);
    obj.function.add(debugRecv);

    EventSendDebugCallAdder.process(obj, names, debugSend);
    EventRecvDebugCallAdder.process(obj, names, debugRecv);

    {// add callback

      for (CompUse use : obj.component) {

        {
          FuncSubHandlerEvent recv = makeRecvProto(sizeType);
          List<Statement> body = makeCode(recv.getName(), recv.param.get(0), recv.param.get(1), recvProto, use.getName());
          recv.body.statements.addAll(body);
          obj.getSubCallback(use).func.add(recv);
        }

        {
          FuncSubHandlerEvent send = makeSendProto(sizeType);
          List<Statement> body = makeCode(send.getName(), send.param.get(0), send.param.get(1), sendProto, use.getName());
          send.body.statements.addAll(body);
          obj.getSubCallback(use).func.add(send);
        }
      }
    }

    return null;
  }

  private FuncCtrlOutDataOut makeMsg(String funcName, String paramName) {
    EvlList<FuncVariable> param = new EvlList<FuncVariable>();
    FuncVariable sender = new FuncVariable(info, paramName, new SimpleRef<Type>(info, arrayType));
    param.add(sender);
    FuncVariable size = new FuncVariable(info, "size", new SimpleRef<Type>(info, sizeType));
    param.add(size);

    FuncCtrlOutDataOut sendFunc = new FuncCtrlOutDataOut(info, funcName, param, new FuncReturnNone(info), new Block(info));
    return sendFunc;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }
}
