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

import ast.Designator;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.ImplComposition;
import ast.data.component.elementary.ImplElementary;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.Plus;
import ast.data.expression.value.NumberValue;
import ast.data.function.header.Procedure;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.Signal;
import ast.data.function.ret.FuncReturnNone;
import ast.data.reference.RefFactory;
import ast.data.reference.RefIndex;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.statement.Assignment;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.Statement;
import ast.data.statement.VarDefStmt;
import ast.data.type.Type;
import ast.data.type.TypeRefFactory;
import ast.data.type.TypeReference;
import ast.data.type.base.ArrayType;
import ast.data.type.base.RangeType;
import ast.data.variable.FunctionVariable;
import ast.dispatcher.NullDispatcher;
import ast.dispatcher.debug.EventRecvDebugCallAdder;
import ast.dispatcher.debug.EventSendDebugCallAdder;

public class DebugIfaceAdder extends NullDispatcher<Void, Void> {
  final private ArrayType arrayType;
  final private RangeType sizeType;
  final private RangeType nameNumType;
  final private ArrayList<String> names;

  public DebugIfaceAdder(ArrayType arrayType, RangeType sizeType, RangeType nameNumType, ArrayList<String> names) {
    super();
    this.names = names;
    this.arrayType = arrayType;
    this.sizeType = sizeType;
    this.nameNumType = nameNumType;
  }

  private FuncSubHandlerEvent makeRecvProto(RangeType sizeType) {
    AstList<FunctionVariable> param = new AstList<FunctionVariable>();
    FunctionVariable sender = new FunctionVariable("receiver", tr(arrayType));
    param.add(sender);
    FunctionVariable size = new FunctionVariable("size", tr(sizeType));
    param.add(size);

    FuncSubHandlerEvent func = new FuncSubHandlerEvent(Designator.NAME_SEP + "msgRecv", param, new FuncReturnNone(), new Block());
    func.body = new Block();
    return func;
  }

  private TypeReference tr(Type type) {
    return TypeRefFactory.create(type);
  }

  private FuncSubHandlerEvent makeSendProto(RangeType sizeType) {
    AstList<FunctionVariable> param = new AstList<FunctionVariable>();
    FunctionVariable sender = new FunctionVariable("sender", tr(arrayType));
    param.add(sender);
    FunctionVariable size = new FunctionVariable("size", tr(sizeType));
    param.add(size);

    FuncSubHandlerEvent func = new FuncSubHandlerEvent(Designator.NAME_SEP + "msgSend", param, new FuncReturnNone(), new Block());
    return func;
  }

  private Procedure makeDebugSend(String callname, Signal sendProto) {
    Block body = new Block();

    FunctionVariable func = new FunctionVariable("func", tr(nameNumType));

    FunctionVariable path = new FunctionVariable("path", tr(arrayType));
    { // path : Array{D,N};
      VarDefStmt def = new VarDefStmt(path);
      body.statements.add(def);
    }

    { // path[0] := func;

      LinkedReferenceWithOffset_Implementation left = RefFactory.oldCreate(path, new RefIndex(new NumberValue(BigInteger.ZERO)));
      LinkedReferenceWithOffset_Implementation right = RefFactory.oldFull(func);
      Assignment ass = new AssignmentSingle(left, new ReferenceExpression(right));
      body.statements.add(ass);
    }

    { // _debug.msgSend( path, 1 );
      ReferenceExpression pathArg = new ReferenceExpression(RefFactory.oldFull(path));
      NumberValue idxArg = new NumberValue(BigInteger.valueOf(1));

      LinkedReferenceWithOffset_Implementation call = RefFactory.oldCall(sendProto, pathArg, idxArg);
      body.statements.add(new CallStmt(call));
    }

    AstList<FunctionVariable> param = new AstList<FunctionVariable>();
    param.add(func);
    Procedure rfunc = new Procedure("_" + callname, param, new FuncReturnNone(), body);

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

  private List<Statement> makeCode(String callname, FunctionVariable pArray, FunctionVariable argSize, Signal proto, String compName) {
    AstList<Statement> code = new AstList<Statement>();

    int x = names.indexOf(compName);
    assert (x >= 0);

    { // sender[size] := x;
      LinkedReferenceWithOffset_Implementation left = RefFactory.oldCreate(pArray, new RefIndex(new ReferenceExpression(RefFactory.oldFull(argSize))));
      NumberValue right = new NumberValue(BigInteger.valueOf(x));
      Assignment ass = new AssignmentSingle(left, right);
      code.add(ass);
    }

    FunctionVariable sizeP1 = new FunctionVariable("sizeP1", tr(sizeType));

    { // sizeP1 := size + 1;
      VarDefStmt def = new VarDefStmt(sizeP1);
      code.add(def);

      Expression expr = new Plus(new ReferenceExpression(RefFactory.oldFull(argSize)), new NumberValue(BigInteger.ONE));
      expr = new TypeCast(TypeRefFactory.create(sizeType), expr);
      Assignment ass = new AssignmentSingle(RefFactory.oldFull(sizeP1), expr);
      code.add(ass);
    }

    { // Self._debug.sendMsg( sender, sizeP1 );
      ReferenceExpression arrayArg = new ReferenceExpression(RefFactory.oldFull(pArray));
      ReferenceExpression sizeArg = new ReferenceExpression(RefFactory.oldFull(sizeP1));

      LinkedReferenceWithOffset_Implementation call = RefFactory.oldCall(proto, arrayArg, sizeArg);
      code.add(new CallStmt(call));
    }

    return code;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {

    Signal sendProto = makeMsg(Designator.NAME_SEP + "msgSend", "sender");
    obj.iface.add(sendProto);
    Signal recvProto = makeMsg(Designator.NAME_SEP + "msgRecv", "receiver");
    obj.iface.add(recvProto);

    Procedure debugSend = makeDebugSend("iMsgSend", sendProto);
    Procedure debugRecv = makeDebugSend("iMsgRecv", recvProto);
    obj.function.add(debugSend);
    obj.function.add(debugRecv);

    EventSendDebugCallAdder.process(obj, names, debugSend);
    EventRecvDebugCallAdder.process(obj, names, debugRecv);

    {// add callback

      for (ComponentUse use : obj.component) {

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

  private Signal makeMsg(String funcName, String paramName) {
    AstList<FunctionVariable> param = new AstList<FunctionVariable>();
    FunctionVariable sender = new FunctionVariable(paramName, tr(arrayType));
    param.add(sender);
    FunctionVariable size = new FunctionVariable("size", tr(sizeType));
    param.add(size);

    Signal sendFunc = new Signal(funcName, param, new FuncReturnNone(), new Block());
    return sendFunc;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }
}
