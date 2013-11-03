package evl.traverser.typecheck.specific;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Pair;

import common.Direction;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.composition.Connection;
import evl.composition.ImplComposition;
import evl.function.FuncIface;
import evl.function.FuncIfaceIn;
import evl.function.FuncIfaceOut;
import evl.function.FunctionBase;
import evl.function.FunctionHeader;
import evl.hfsm.ImplHfsm;
import evl.hfsm.Transition;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.ImplElementary;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;
import evl.traverser.ClassGetter;
import evl.traverser.typecheck.LeftIsContainerOfRightTest;
import evl.traverser.typecheck.TypeGetter;
import evl.type.Type;
import evl.variable.Constant;

public class CompInterfaceTypeChecker extends NullTraverser<Void, Void> {

  private KnowledgeBase kb;

  public CompInterfaceTypeChecker(KnowledgeBase kb) {
    super();
    this.kb = kb;
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
    checkCallback(obj.getComponent(), obj.getSubComCallback());
    checkInput(obj.getInput(), obj.getInternalFunction());
    return null;
  }

  @Override
  protected Void visitType(Type obj, Void param) {
    return null;
  }

  private void checkCallback(ListOfNamed<CompUse> complist, ListOfNamed<NamedList<FunctionHeader>> cblist) {
    Set<Pair<CompUse, NamedList<FunctionHeader>>> map = nameMap(complist.getList(), cblist.getList());

    for (Pair<CompUse, NamedList<FunctionHeader>> use : map) {
      assert ((use.first != null) || (use.second != null));
      if (use.first == null) {
        FunctionHeader func = use.second.getList().get(0);
        RError.err(ErrorType.Error, func.getInfo(), "No component defined with name " + use.second.getName());
      }
      Component comp = use.first.getLink();

      if (!comp.getOutput().isEmpty()) {
        if (use.second == null) { // OK
          RError.err(ErrorType.Error, use.first.getInfo(), "No callback functions found for component " + use.first.getName());
        }
        checkCb(use.first.getInfo(), use.first.getName(), comp.getOutput(), use.second);
      }
    }
  }

  private void checkCb(ElementInfo info, String compname, ListOfNamed<FuncIfaceOut> listOfNamed, NamedList<FunctionHeader> inputFunc) {
    Set<Pair<FuncIfaceOut, FunctionHeader>> map = nameMap(listOfNamed.getList(), inputFunc.getList());

    for (Pair<FuncIfaceOut, FunctionHeader> itr : map) {
      assert ((itr.first != null) || (itr.second != null));
      if (itr.first == null) {
        assert (itr.second != null);
        FunctionHeader func = itr.second;
        RError.err(ErrorType.Error, func.getInfo(), "Component " + compname + " has no output function " + itr.second.getName());
      }
      if (itr.second == null) {
        RError.err(ErrorType.Error, info, "Missing callback for component/function " + compname + "." + itr.first.getName());
      }

      Type prottype = TypeGetter.process(itr.first);
      Type impltype = TypeGetter.process(itr.second);
      if (!LeftIsContainerOfRightTest.process(prottype, impltype, kb)) {
        RError.err(ErrorType.Error, itr.second.getInfo(), "Function does not implement prototype: " + itr.first);
      }
    }
  }

  private void checkInput(ListOfNamed<FuncIfaceIn> listOfNamed, ListOfNamed<FunctionHeader> inputFunc) {
    for (FuncIfaceIn proto : listOfNamed) {
      FunctionHeader impl = inputFunc.find(proto.getName());
      if (impl == null) {
        RError.err(ErrorType.Error, proto.getInfo(), "Missing function implementation " + proto.getName());
      } else {
        Type prottype = TypeGetter.process(proto);
        Type impltype = TypeGetter.process(impl);
        if (!LeftIsContainerOfRightTest.process(prottype, impltype, kb)) {
          RError.err(ErrorType.Error, impl.getInfo(), "Function does not implement prototype: " + proto);
        }
      }
    }

    /*
     * Set<Pair<FuncIfaceIn, FunctionBase>> map = nameMap(listOfNamed.getList(), inputFunc.getList()); // OK
     * checkList(map, "No interface with the name ", "Missing functions of interface ");
     * 
     * for( Pair<FuncIfaceIn, FunctionBase> itr : map ) { Interface iface = itr.first.getLink();
     * ListOfNamed<FunctionBase> prototype = iface.getPrototype();
     * 
     * // OK checkFunctions(itr.first.getInfo(), itr.first.getName(), itr.second, prototype); }
     */
  }

  private void checkFunctions(ElementInfo info, String name, ListOfNamed<FunctionBase> impl, ListOfNamed<FunctionBase> type) {
    Set<Pair<FunctionBase, FunctionBase>> map = nameMap(type.getList(), impl.getList());

    for (Pair<FunctionBase, FunctionBase> itr : map) {
      assert ((itr.first != null) || (itr.second != null));
      if (itr.first == null) {
        RError.err(ErrorType.Error, itr.second.getInfo(), "interface " + name + " has no function " + itr.second.getName());
      }
      if (itr.second == null) {
        RError.err(ErrorType.Error, info, "missing function implementation " + name + "." + itr.first.getName());
      }

      Type prottype = TypeGetter.process(itr.first);
      Type impltype = TypeGetter.process(itr.second);
      if (!LeftIsContainerOfRightTest.process(prottype, impltype, kb)) {
        RError.err(ErrorType.Error, itr.second.getInfo(), "Function does not implement prototype: " + itr.first);
      }
    }
  }

  private <T extends Named, S extends Named> Set<Pair<T, S>> nameMap(Collection<T> a, Collection<S> b) {
    Set<String> names = new HashSet<String>();
    Map<String, T> amap = new HashMap<String, T>();
    Map<String, S> bmap = new HashMap<String, S>();
    Set<Pair<T, S>> ret = new HashSet<Pair<T, S>>();
    for (T itr : a) {
      names.add(itr.getName());
      amap.put(itr.getName(), itr);
    }
    for (S itr : b) {
      names.add(itr.getName());
      bmap.put(itr.getName(), itr);
    }
    for (String name : names) {
      Pair<T, S> pair = new Pair<T, S>(amap.get(name), bmap.get(name));
      ret.add(pair);
    }
    return ret;
  }

  private <T extends Named, S extends Named> void checkList(Set<Pair<T, S>> items, String firstmsg, String secondmsg) {
    for (Pair<T, S> item : items) {
      assert ((item.first != null) || (item.second != null));
      if (item.first == null) {
        RError.err(ErrorType.Error, item.second.getInfo(), firstmsg + item.second.getName());
      }
      if (item.second == null) {
        RError.err(ErrorType.Error, item.first.getInfo(), secondmsg + item.first.getName());
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
