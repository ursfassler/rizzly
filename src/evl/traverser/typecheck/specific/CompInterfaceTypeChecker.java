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
import evl.function.FunctionBase;
import evl.hfsm.ImplHfsm;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.IfaceUse;
import evl.other.ImplElementary;
import evl.other.Interface;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;
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
    return null; // TODO check if all queries are defined
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    checkCallback(obj.getComponent(), obj.getSubComCallback());
    checkInput(obj.getIface(Direction.in), obj.getInputFunc());
    return null;
  }

  @Override
  protected Void visitInterface(Interface obj, Void param) {
    return null;
  }

  @Override
  protected Void visitType(Type obj, Void param) {
    return null;
  }

  private void checkCallback(ListOfNamed<CompUse> complist, ListOfNamed<NamedList<NamedList<FunctionBase>>> cblist) {
    Set<Pair<CompUse, NamedList<NamedList<FunctionBase>>>> map = nameMap(complist.getList(), cblist.getList());

    for (Pair<CompUse, NamedList<NamedList<FunctionBase>>> use : map) {
      assert ((use.first != null) || (use.second != null));
      if (use.first == null) {
        NamedList<FunctionBase> item = use.second.getList().get(0);
        assert (!item.isEmpty());
        FunctionBase func = item.getList().get(0);
        // OK
        RError.err(ErrorType.Error, func.getInfo(), "No component defined with name " + use.second.getName());
      }
      Component comp = use.first.getLink();

      if (!comp.getIface(Direction.out).isEmpty()) {
        if (use.second == null) {
          // OK
          RError.err(ErrorType.Error, use.first.getInfo(), "No callback functions found for component " + use.first.getName());
        }
        checkCb(use.first.getInfo(), use.first.getName(), comp.getIface(Direction.out), use.second);
      }
    }
  }

  private void checkCb(ElementInfo info, String compname, ListOfNamed<IfaceUse> listOfNamed, ListOfNamed<NamedList<FunctionBase>> inputFunc) {
    Set<Pair<IfaceUse, NamedList<FunctionBase>>> map = nameMap(listOfNamed.getList(), inputFunc.getList());

    for (Pair<IfaceUse, NamedList<FunctionBase>> itr : map) {
      assert ((itr.first != null) || (itr.second != null));
      if (itr.first == null) {
        assert (!itr.second.isEmpty());
        FunctionBase func = itr.second.getList().get(0);
        // OK
        RError.err(ErrorType.Error, func.getInfo(), "Component " + compname + " has no output interface " + itr.second.getName());
      }
      if (itr.second == null) {
        // OK
        RError.err(ErrorType.Error, info, "Missing callback for component/interface " + compname + "." + itr.first.getName());
      }

      Interface iface = itr.first.getLink();
      ListOfNamed<FunctionBase> prototype = iface.getPrototype();

      // OK
      checkFunctions(itr.second.getInfo(), compname + "." + itr.first.getName(), itr.second, prototype);
    }
  }

  private void checkInput(ListOfNamed<IfaceUse> listOfNamed, ListOfNamed<NamedList<FunctionBase>> inputFunc) {
    Set<Pair<IfaceUse, NamedList<FunctionBase>>> map = nameMap(listOfNamed.getList(), inputFunc.getList());
    // OK
    checkList(map, "No interface with the name ", "Missing functions of interface ");

    for (Pair<IfaceUse, NamedList<FunctionBase>> itr : map) {
      Interface iface = itr.first.getLink();
      ListOfNamed<FunctionBase> prototype = iface.getPrototype();

      // OK
      checkFunctions(itr.first.getInfo(), itr.first.getName(), itr.second, prototype);
    }
  }

  private void checkFunctions(ElementInfo info, String name, ListOfNamed<FunctionBase> impl, ListOfNamed<FunctionBase> type) {
    Set<Pair<FunctionBase, FunctionBase>> map = nameMap(type.getList(), impl.getList());

    for (Pair<FunctionBase, FunctionBase> itr : map) {
      assert ((itr.first != null) || (itr.second != null));
      if (itr.first == null) {
        // OK
        RError.err(ErrorType.Error, itr.second.getInfo(), "interface " + name + " has no function " + itr.second.getName());
      }
      if (itr.second == null) {
        // OK
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
    for (IfaceUse ifaceuse : obj.getIface(dir)) {
      if (!ifaceIsConnected(ifaceuse, dir, obj.getConnection())) {
        RError.err(ErrorType.Error, ifaceuse.getInfo(), "Interface " + ifaceuse.getName() + " not connected");
      }
    }
  }

  private Component checkIface(ImplComposition obj, CompUse use, Direction dir) {
    Component type = use.getLink();
    for (IfaceUse ifaceuse : type.getIface(dir)) {
      if (!ifaceIsConnected(ifaceuse, dir.other(), obj.getConnection())) {
        RError.err(ErrorType.Error, use.getInfo(), "Interface " + use.getName() + "." + ifaceuse.getName() + " not connected");
      }
    }
    return type;
  }

  private boolean ifaceIsConnected(IfaceUse ifaceuse, Direction dir, List<Connection> connection) {
    for (Connection itr : connection) {
      IfaceUse src = itr.getEndpoint(dir).getIfaceUse();
      if (src == ifaceuse) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected Void visitConnection(Connection obj, Void param) {
    Interface srcType = obj.getEndpoint(Direction.in).getIfaceUse().getLink();
    Interface dstType = obj.getEndpoint(Direction.out).getIfaceUse().getLink();
    if (!srcType.equals(dstType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Incompatible interfaces: " + srcType.getName() + " " + obj.getType() + " " + dstType.getName());
    }
    return null;
  }

}
