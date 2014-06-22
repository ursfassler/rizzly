package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.Collections;

import common.Designator;
import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.composition.ImplComposition;
import evl.copy.Copy;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FunctionBase;
import evl.function.impl.FuncIfaceInVoid;
import evl.function.impl.FuncImplSlot;
import evl.hfsm.ImplHfsm;
import evl.other.CompUse;
import evl.other.ImplElementary;
import evl.other.ListOfNamed;
import evl.other.Namespace;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.variable.FuncVariable;

public class SystemIfaceAdder extends NullTraverser<Void, Void> {

  @Deprecated
  public static final String IFACE_USE_NAME = "_system";
  @Deprecated
  public static final String IFACE_TYPE_NAME = "_System";
  public static final String DESTRUCT = Designator.NAME_SEP + "destruct";
  public static final String CONSTRUCT = Designator.NAME_SEP + "construct";
  final private FuncIfaceInVoid sendFunc;
  final private FuncIfaceInVoid recvFunc;
  static private ElementInfo info = new ElementInfo();

  public SystemIfaceAdder(FuncIfaceInVoid sendFunc, FuncIfaceInVoid recvFunc) {
    super();
    this.sendFunc = sendFunc;
    this.recvFunc = recvFunc;
  }

  public static void process(FuncIfaceInVoid sendFunc, FuncIfaceInVoid recvFunc, Evl obj) {
    SystemIfaceAdder reduction = new SystemIfaceAdder(sendFunc, recvFunc);
    reduction.traverse(obj, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    return null;
    // throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    throw new RuntimeException("Forgot hfsm reduction phase, dude");
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    throw new RuntimeException("Forgot composition reduction phase, dude");
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    { // add iface
      FuncIfaceInVoid recv = Copy.copy(recvFunc);
      FuncIfaceInVoid send = Copy.copy(sendFunc);
      obj.getSlot().add(recv);
      obj.getSlot().add(send);
    }

    ArrayList<CompUse> compList = new ArrayList<CompUse>(obj.getComponent().getList());
    // FIXME this order may cause errors as it is not granted to be topological order

    FuncImplSlot ctor = makeFunc(CONSTRUCT);
    FuncImplSlot dtor = makeFunc(DESTRUCT);

    {
      ArrayList<Statement> code = new ArrayList<Statement>();
      for (CompUse cuse : compList) {
        CallStmt call = makeCall(cuse, CONSTRUCT); // TODO correct link? Or should it be to the instance?
        code.add(call);
      }
      code.add(makeCall(obj.getEntryFunc()));

      ctor.getBody().getStatements().addAll(code);
    }

    {
      ArrayList<Statement> code = new ArrayList<Statement>();
      code.add(makeCall(obj.getExitFunc()));
      Collections.reverse(compList);
      for (CompUse cuse : compList) {
        CallStmt call = makeCall(cuse, DESTRUCT); // TODO correct link? Or should it be to the instance?
        code.add(call);
      }

      dtor.getBody().getStatements().addAll(code);
    }

    obj.getFunction().add(ctor);
    obj.getFunction().add(dtor);

    return null;
  }

  private CallStmt makeCall(Reference ref) {
    assert (ref.getOffset().isEmpty());
    assert (ref.getLink() instanceof FunctionBase);
    Reference call = new Reference(ref.getInfo(), ref.getLink());
    call.getOffset().add(new RefCall(info, new ArrayList<Expression>()));
    return new CallStmt(info, call);
  }

  private FuncImplSlot makeFunc(String funcname) {
    FuncImplSlot rfunc = new FuncImplSlot(info, funcname, new ListOfNamed<FuncVariable>());
    Block body = new Block(info);
    rfunc.setBody(body);
    return rfunc;
  }

  private CallStmt makeCall(CompUse self, String funcname) {
    Reference fref = new Reference(info, self);
    // fref.getOffset().add(new RefName(info, IFACE_USE_NAME));
    fref.getOffset().add(new RefName(info, funcname));
    fref.getOffset().add(new RefCall(info, new ArrayList<Expression>()));
    return new CallStmt(info, fref);
  }
}
