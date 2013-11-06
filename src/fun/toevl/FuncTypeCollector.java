package fun.toevl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import common.Direction;

import error.ErrorType;
import error.RError;
import evl.function.FunctionBase;
import evl.function.impl.FuncIfaceInRet;
import evl.function.impl.FuncIfaceInVoid;
import evl.function.impl.FuncIfaceOutRet;
import evl.function.impl.FuncIfaceOutVoid;
import evl.function.impl.FuncInputHandlerEvent;
import evl.function.impl.FuncInputHandlerQuery;
import evl.function.impl.FuncPrivateRet;
import evl.function.impl.FuncPrivateVoid;
import evl.function.impl.FuncSubHandlerEvent;
import evl.function.impl.FuncSubHandlerQuery;
import evl.hfsm.HfsmQueryFunction;
import fun.DefTraverser;
import fun.function.FuncWithReturn;
import fun.function.FunctionHeader;
import fun.function.impl.FuncGlobal;
import fun.hfsm.State;
import fun.other.Component;
import fun.other.ImplElementary;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.other.Namespace;

public class FuncTypeCollector extends DefTraverser<Void, Set<String>> {
  private Map<FunctionHeader, Class<? extends FunctionBase>> funcType = new HashMap<FunctionHeader, Class<? extends FunctionBase>>();

  public static Map<FunctionHeader, Class<? extends FunctionBase>> process(Namespace classes) {
    FuncTypeCollector collector = new FuncTypeCollector();
    collector.traverse(classes, null);
    return collector.funcType;
  }

  private void add(FunctionHeader func, Class<? extends FunctionBase> kind) {
    assert (!funcType.containsKey(func));
    funcType.put(func, kind);
  }

  @Override
  protected Void visitFuncGlobal(FuncGlobal obj, Set<String> param) {
    add(obj, evl.function.impl.FuncGlobal.class);
    return null;
  }

  @Override
  protected Void visitState(State obj, Set<String> param) {
    assert (param != null);
    for (FunctionHeader use : obj.getItemList().getItems(FunctionHeader.class)) {
      Class<? extends FunctionBase> kind;
      if (param.contains(use.getName())) {
        assert (use instanceof FuncWithReturn); // events are captured by transitions //TODO make nice error message
        kind = HfsmQueryFunction.class;
      } else {
        kind = use instanceof FuncWithReturn ? evl.function.impl.FuncPrivateRet.class : evl.function.impl.FuncPrivateVoid.class;
      }
      add(use, kind);
    }
    return super.visitState(obj, param);
  }

  @Override
  protected Void visitComponent(Component obj, Set<String> param) {
    assert (param == null);
    param = new HashSet<String>();
    for (FunctionHeader func : obj.getIface(Direction.in)) {
      Class<? extends evl.function.FunctionBase> kind = func instanceof FuncWithReturn ? FuncIfaceInRet.class : FuncIfaceInVoid.class;
      add(func, kind);
      param.add(func.getName());
    }
    for (FunctionHeader func : obj.getIface(Direction.out)) {
      Class<? extends evl.function.FunctionBase> kind = func instanceof FuncWithReturn ? FuncIfaceOutRet.class : FuncIfaceOutVoid.class;
      add(func, kind);
    }
    return super.visitComponent(obj, param);
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Set<String> param) {
    // visitItr(obj.getIface(Direction.in), param);
    // visitItr(obj.getIface(Direction.out), param);
    trfunc(0, obj.getFunction(), param);
    return null;
  }

  private void trfunc(int names, ListOfNamed<Named> func, Set<String> param) {
    for (Named item : func) {
      if (item instanceof Namespace) {
        trfunc(names + 1, (Namespace) item, param);
      } else {
        Class<? extends evl.function.FunctionBase> kind;
        switch (names) {
        case 0:
          if (param.contains(item.getName())) {
            kind = item instanceof FuncWithReturn ? FuncInputHandlerQuery.class : FuncInputHandlerEvent.class;
          } else {
            kind = item instanceof FuncWithReturn ? FuncPrivateRet.class : FuncPrivateVoid.class;
          }
          break;
        case 1:
          kind = item instanceof FuncWithReturn ? FuncSubHandlerQuery.class : FuncSubHandlerEvent.class;
          break;
        default:
          RError.err(ErrorType.Error, item.getInfo(), "Too deep namespace: " + names + "; " + item.getName());
          return;
        }
        fun.function.FunctionHeader cfun = (fun.function.FunctionHeader) item;
        add(cfun, kind);
      }
    }
  }
}
