package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import common.Direction;
import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.composition.ImplComposition;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FunctionBase;
import evl.function.impl.FuncInputHandlerEvent;
import evl.hfsm.ImplHfsm;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.IfaceUse;
import evl.other.ImplElementary;
import evl.other.Interface;
import evl.other.ListOfNamed;
import evl.other.Namespace;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.variable.FuncVariable;

public class SystemIfaceAdder extends NullTraverser<Void, Void> {
  public static final String IFACE_USE_NAME = "_system";
  public static final String IFACE_TYPE_NAME = "_System";
  public static final String DESTRUCT = "destruct";
  public static final String CONSTRUCT = "construct";
  static private ElementInfo info = new ElementInfo();
  private KnowBaseItem kbi;

  public SystemIfaceAdder(KnowledgeBase kb) {
    super();
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  public static void process(Evl obj, KnowledgeBase kb) {
    SystemIfaceAdder reduction = new SystemIfaceAdder(kb);
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
    return null; // added during reduction to elementary
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    throw new RuntimeException("Fool! you forgot the composition reduction phase, dude.");
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    { // add iface
      Interface iface = kbi.get(Interface.class, IFACE_TYPE_NAME);
      IfaceUse debIface = new IfaceUse(info, IFACE_USE_NAME, iface);
      obj.getIface(Direction.in).add(debIface);
    }

    FuncInputHandlerEvent ctor = makeFunc(CONSTRUCT);
    ArrayList<CompUse> compList = new ArrayList<CompUse>(obj.getComponent().getList());
    // FIXME this order may cause errors as it is not granted to be topological order
    for (CompUse cuse : compList) {
      CallStmt call = makeCall(cuse, CONSTRUCT); // TODO correct link? Or should it be to the instance?
      ctor.getBody().getStatements().add(call);
    }
    ctor.getBody().getStatements().add(makeCall(obj.getEntryFunc()));

    FuncInputHandlerEvent dtor = makeFunc(DESTRUCT);
    dtor.getBody().getStatements().add(makeCall(obj.getExitFunc()));
    Collections.reverse(compList);
    for (CompUse cuse : compList) {
      CallStmt call = makeCall(cuse, DESTRUCT); // TODO correct link? Or should it be to the instance?
      dtor.getBody().getStatements().add(call);
    }

    List<String> ns = new ArrayList<String>();
    ns.add(IFACE_USE_NAME);
    obj.addFunction(ns, ctor);
    obj.addFunction(ns, dtor);

    return null;
  }

  private Statement makeCall(Reference ref) {
    assert (ref.getOffset().isEmpty());
    assert (ref.getLink() instanceof FunctionBase);
    Reference call = new Reference(ref.getInfo(), ref.getLink());
    call.getOffset().add(new RefCall(info, new ArrayList<Expression>()));
    return new CallStmt(info, call);
  }

  private FuncInputHandlerEvent makeFunc(String funcname) {
    FuncInputHandlerEvent rfunc = new FuncInputHandlerEvent(info, funcname, new ListOfNamed<FuncVariable>());
    rfunc.setBody(new Block(info));
    return rfunc;
  }

  private CallStmt makeCall(CompUse self, String funcname) {
    Reference fref = new Reference(info, self);
    fref.getOffset().add(new RefName(info, IFACE_USE_NAME));
    fref.getOffset().add(new RefName(info, funcname));
    fref.getOffset().add(new RefCall(info, new ArrayList<Expression>()));
    return new CallStmt(info, fref);
  }

}
