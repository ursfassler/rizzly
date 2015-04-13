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

package ast.pass.debug;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.composition.CompUse;
import ast.data.component.composition.ImplComposition;
import ast.data.component.elementary.ImplElementary;
import ast.data.expression.Expression;
import ast.data.expression.Number;
import ast.data.expression.TupleValue;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.Plus;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.RefIndex;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.expression.reference.TypeRef;
import ast.data.function.header.FuncProcedure;
import ast.data.function.header.FuncSignal;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.ret.FuncReturnNone;
import ast.data.statement.Assignment;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.Statement;
import ast.data.statement.VarDefStmt;
import ast.data.type.Type;
import ast.data.type.base.ArrayType;
import ast.data.type.base.RangeType;
import ast.data.type.special.VoidType;
import ast.data.variable.FuncVariable;
import ast.traverser.NullTraverser;
import ast.traverser.debug.EventRecvDebugCallAdder;
import ast.traverser.debug.EventSendDebugCallAdder;

import common.Designator;
import common.ElementInfo;

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
    AstList<FuncVariable> param = new AstList<FuncVariable>();
    FuncVariable sender = new FuncVariable(info, "receiver", tr(arrayType));
    param.add(sender);
    FuncVariable size = new FuncVariable(info, "size", tr(sizeType));
    param.add(size);

    FuncSubHandlerEvent func = new FuncSubHandlerEvent(info, Designator.NAME_SEP + "msgRecv", param, new FuncReturnNone(info), new Block(info));
    func.body = new Block(info);
    return func;
  }

  private TypeRef tr(Type type) {
    return new SimpleRef<Type>(info, type);
  }

  private FuncSubHandlerEvent makeSendProto(RangeType sizeType) {
    AstList<FuncVariable> param = new AstList<FuncVariable>();
    FuncVariable sender = new FuncVariable(info, "sender", tr(arrayType));
    param.add(sender);
    FuncVariable size = new FuncVariable(info, "size", tr(sizeType));
    param.add(size);

    FuncSubHandlerEvent func = new FuncSubHandlerEvent(info, Designator.NAME_SEP + "msgSend", param, new FuncReturnNone(info), new Block(info));
    return func;
  }

  private FuncProcedure makeDebugSend(String callname, FuncSignal sendProto) {
    Block body = new Block(info);

    FuncVariable func = new FuncVariable(info, "func", tr(nameNumType));

    FuncVariable path = new FuncVariable(info, "path", tr(arrayType));
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

    AstList<FuncVariable> param = new AstList<FuncVariable>();
    param.add(func);
    FuncProcedure rfunc = new FuncProcedure(info, "_" + callname, param, new FuncReturnNone(info), body);

    return rfunc;
  }

  @Override
  protected Void visitDefault(Ast obj, Void param) {
    // what now?
    return null;
    // throw new RuntimeException("not yet implemented: " +
    // obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitList(obj.children, param);
    return null;
  }

  private List<Statement> makeCode(String callname, FuncVariable pArray, FuncVariable argSize, FuncSignal proto, String compName) {
    AstList<Statement> code = new AstList<Statement>();

    int x = names.indexOf(compName);
    assert (x >= 0);

    { // sender[size] := x;
      Reference left = new Reference(info, pArray, new RefIndex(info, new Reference(info, argSize)));
      Number right = new Number(info, BigInteger.valueOf(x));
      Assignment ass = new AssignmentSingle(info, left, right);
      code.add(ass);
    }

    FuncVariable sizeP1 = new FuncVariable(info, "sizeP1", tr(sizeType));

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

    FuncSignal sendProto = makeMsg(Designator.NAME_SEP + "msgSend", "sender");
    obj.iface.add(sendProto);
    FuncSignal recvProto = makeMsg(Designator.NAME_SEP + "msgRecv", "receiver");
    obj.iface.add(recvProto);

    FuncProcedure debugSend = makeDebugSend("iMsgSend", sendProto);
    FuncProcedure debugRecv = makeDebugSend("iMsgRecv", recvProto);
    obj.function.add(debugSend);
    obj.function.add(debugRecv);

    EventSendDebugCallAdder.process(obj, names, debugSend);
    EventRecvDebugCallAdder.process(obj, names, debugRecv);

    {// add callback

      for (CompUse use : obj.component) {

        {
          FuncSubHandlerEvent recv = makeRecvProto(sizeType);
          List<Statement> body = makeCode(recv.name, recv.param.get(0), recv.param.get(1), recvProto, use.name);
          recv.body.statements.addAll(body);
          obj.getSubCallback(use).func.add(recv);
        }

        {
          FuncSubHandlerEvent send = makeSendProto(sizeType);
          List<Statement> body = makeCode(send.name, send.param.get(0), send.param.get(1), sendProto, use.name);
          send.body.statements.addAll(body);
          obj.getSubCallback(use).func.add(send);
        }
      }
    }

    return null;
  }

  private FuncSignal makeMsg(String funcName, String paramName) {
    AstList<FuncVariable> param = new AstList<FuncVariable>();
    FuncVariable sender = new FuncVariable(info, paramName, tr(arrayType));
    param.add(sender);
    FuncVariable size = new FuncVariable(info, "size", tr(sizeType));
    param.add(size);

    FuncSignal sendFunc = new FuncSignal(info, funcName, param, new FuncReturnNone(info), new Block(info));
    return sendFunc;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }
}
