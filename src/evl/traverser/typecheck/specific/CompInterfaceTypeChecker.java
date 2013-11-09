package evl.traverser.typecheck.specific;

import java.util.List;

import common.Direction;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.composition.Connection;
import evl.composition.ImplComposition;
import evl.function.FuncIface;
import evl.function.FuncIfaceIn;
import evl.function.FunctionBase;
import evl.function.FunctionHeader;
import evl.hfsm.ImplHfsm;
import evl.hfsm.Transition;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.ImplElementary;
import evl.other.ListOfNamed;
import evl.other.Namespace;
import evl.traverser.ClassGetter;
import evl.traverser.typecheck.LeftIsContainerOfRightTest;
import evl.type.Type;
import evl.variable.Constant;

public class CompInterfaceTypeChecker extends NullTraverser<Void, Void> {

  private KnowledgeBase kb;
  private KnowType kt;

  public CompInterfaceTypeChecker(KnowledgeBase kb) {
    super();
    this.kb = kb;
    this.kt = kb.getEntry(KnowType.class);
  }

  public static void process(Namespace impl, KnowledgeBase kb) {
    CompInterfaceTypeChecker adder = new CompInterfaceTypeChecker(kb);
    adder.visitItr(impl, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Void sym) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Void param) {
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    // TODO can we merge with test for elementary?
    // TODO test also other stuff?
    List<Transition> transList = ClassGetter.get(Transition.class, obj);

    for (Transition tr : transList) {
      // TODO check if tr.getEventFunc() has compatible parameters
    }

    return null; // TODO check if all queries are defined
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    assert (obj.getComponent().isEmpty());
    assert (obj.getSubCallback().isEmpty());
    checkInput(obj.getInput(), obj.getFunction());
    return null;
  }

  @Override
  protected Void visitType(Type obj, Void param) {
    return null;
  }

  private void checkInput(ListOfNamed<FuncIfaceIn> listOfNamed, ListOfNamed<FunctionHeader> inputFunc) {
    for (FuncIfaceIn proto : listOfNamed) {
      FunctionHeader impl = inputFunc.find(proto.getName());
      if (impl == null) {
        RError.err(ErrorType.Error, proto.getInfo(), "Missing function implementation " + proto.getName());
      } else {
        Type prottype = kt.get(proto);
        Type impltype = kt.get(impl);
        if (!LeftIsContainerOfRightTest.process(prottype, impltype, kb)) {
          RError.err(ErrorType.Error, impl.getInfo(), "Function does not implement prototype: " + proto);
        }
      }
    }

  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    // TODO do checks over whole implementation, i.e. not splitting when functions has return value
    // TODO check for cycles
    visitItr(obj.getConnection(), param);

    checkSelfIface(obj, Direction.out);
    checkSelfIface(obj, Direction.in);

    for (CompUse use : obj.getComponent()) {
      checkIface(obj, use, Direction.in);
      checkIface(obj, use, Direction.out);
    }

    return null;
  }

  private void checkSelfIface(ImplComposition obj, Direction dir) {
    for (FuncIface ifaceuse : obj.getIface(dir)) {
      if (!ifaceIsConnected(ifaceuse, dir, obj.getConnection())) {
        RError.err(ErrorType.Error, ifaceuse.getInfo(), "Interface " + ifaceuse.getName() + " not connected");
      }
    }
  }

  private Component checkIface(ImplComposition obj, CompUse use, Direction dir) {
    Component type = use.getLink();
    for (FuncIface ifaceuse : type.getIface(dir)) {
      if (!ifaceIsConnected(ifaceuse, dir.other(), obj.getConnection())) {
        RError.err(ErrorType.Error, use.getInfo(), "Interface " + use.getName() + "." + ifaceuse.getName() + " not connected");
      }
    }
    return type;
  }

  private boolean ifaceIsConnected(FuncIface ifaceuse, Direction dir, List<Connection> connection) {
    for (Connection itr : connection) {
      FuncIface src = itr.getEndpoint(dir).getIfaceUse();
      if (src == ifaceuse) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected Void visitConnection(Connection obj, Void param) {
    FuncIface srcType = obj.getEndpoint(Direction.in).getIfaceUse();
    FuncIface dstType = obj.getEndpoint(Direction.out).getIfaceUse();
    // TODO check if functions are compatible
    return null;
  }
}
