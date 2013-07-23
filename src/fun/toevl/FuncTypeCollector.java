package fun.toevl;

import java.util.HashMap;
import java.util.Map;

import error.ErrorType;
import error.RError;
import evl.function.FunctionBase;
import evl.function.impl.FuncInputHandlerEvent;
import evl.function.impl.FuncInputHandlerQuery;
import evl.function.impl.FuncPrivateRet;
import evl.function.impl.FuncPrivateVoid;
import evl.function.impl.FuncProtoRet;
import evl.function.impl.FuncProtoVoid;
import evl.function.impl.FuncSubHandlerEvent;
import evl.function.impl.FuncSubHandlerQuery;
import evl.hfsm.HfsmQueryFunction;
import fun.DefGTraverser;
import fun.function.FuncWithReturn;
import fun.function.FunctionHeader;
import fun.function.impl.FuncGlobal;
import fun.hfsm.QueryItem;
import fun.hfsm.State;
import fun.other.ImplElementary;
import fun.other.Interface;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.other.Namespace;

public class FuncTypeCollector extends DefGTraverser<Void, Void> {
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
  protected Void visitFuncGlobal(FuncGlobal obj, Void param) {
    add( obj, evl.function.impl.FuncGlobal.class );
    return null;
  }

  @Override
  protected Void visitState(State obj, Void param) {
    for (FunctionHeader use : obj.getBfunc()) {
      Class<? extends FunctionBase> kind = use instanceof FuncWithReturn ? evl.function.impl.FuncPrivateRet.class : evl.function.impl.FuncPrivateVoid.class;
      add(use, kind);
    }
    visitItr(obj.getItem(), null);
    return null;
  }

  @Override
  protected Void visitQueryItem(QueryItem obj, Void param) {
    add(obj.getFunc(), HfsmQueryFunction.class);
    return null;
  }

  @Override
  protected Void visitInterface(Interface obj, Void param) {
    for (FunctionHeader func : obj.getPrototype()) {
      add(func, func instanceof FuncWithReturn ? FuncProtoRet.class : FuncProtoVoid.class);
    }
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    trfunc(0, obj.getFunction());
    return null;
  }

  private void trfunc(int names, ListOfNamed<Named> func) {
    for (Named item : func) {
      if (item instanceof Namespace) {
        trfunc(names + 1, (Namespace) item);
      } else {
        Class<? extends evl.function.FunctionBase> kind;
        switch (names) {
        case 0:
          kind = item instanceof FuncWithReturn ? FuncPrivateRet.class : FuncPrivateVoid.class;
          break;
        case 1:
          kind = item instanceof FuncWithReturn ? FuncInputHandlerQuery.class : FuncInputHandlerEvent.class;
          break;
        case 2:
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
