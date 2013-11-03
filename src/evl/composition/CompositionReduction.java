package evl.composition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Pair;

import common.Direction;
import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.copy.Copy;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FuncIface;
import evl.function.FuncIfaceOut;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.function.FunctionFactory;
import evl.function.FunctionHeader;
import evl.function.impl.FuncInputHandlerEvent;
import evl.function.impl.FuncInputHandlerQuery;
import evl.function.impl.FuncPrivateVoid;
import evl.function.impl.FuncSubHandlerEvent;
import evl.function.impl.FuncSubHandlerQuery;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.ImplElementary;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.other.Namespace;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.ReturnExpr;
import evl.variable.FuncVariable;
import evl.variable.Variable;
import fun.hfsm.State;

//TODO cleanup
public class CompositionReduction extends NullTraverser<Named, Void> {

  private static final ElementInfo info = new ElementInfo();
  private Map<ImplComposition, ImplElementary> map = new HashMap<ImplComposition, ImplElementary>();

  public static Map<ImplComposition, ImplElementary> process(Namespace classes) {
    CompositionReduction reduction = new CompositionReduction();
    reduction.visitItr(classes, null);
    return reduction.map;
  }

  @Override
  protected Named visitDefault(Evl obj, Void param) {
    assert (obj instanceof Named);
    return (Named) obj;
  }

  @Override
  protected Named visitNamespace(Namespace obj, Void param) {
    for (int i = 0; i < obj.getList().size(); i++) {
      Named item = obj.getList().get(i);
      item = visit(item, null);
      assert (item != null);
      obj.getList().set(i, item);
    }
    return obj;
  }

  @Override
  protected ImplElementary visitImplComposition(ImplComposition obj, Void param) {
    ImplElementary elem = new ImplElementary(obj.getInfo(), obj.getName());
    elem.getComponent().addAll(obj.getComponent());
    elem.getInput().addAll(obj.getInput());
    elem.getOutput().addAll(obj.getOutput());

    elem.setEntryFunc(makeEntryExitFunc(State.ENTRY_FUNC_NAME, elem.getInternalFunction()));
    elem.setExitFunc(makeEntryExitFunc(State.EXIT_FUNC_NAME, elem.getInternalFunction()));

    Map<FuncIface, List<Endpoint>> input = new HashMap<FuncIface, List<Endpoint>>();
    Map<Pair<CompUse, String>, List<Endpoint>> callback = new HashMap<Pair<CompUse, String>, List<Endpoint>>();
    for (Connection con : obj.getConnection()) {
      assert (con.getType() == MessageType.sync);
      Endpoint src = con.getEndpoint(Direction.in);

      if (src instanceof EndpointSelf) {
        FuncIface srcIface = ((EndpointSelf) src).getIface();
        List<Endpoint> set = input.get(srcIface);
        if (set == null) {
          set = new ArrayList<Endpoint>();
        }
        set.add(con.getEndpoint(Direction.out));
        input.put(srcIface, set);
      } else {
        CompUse srcComp = ((EndpointSub) src).getComp();
        String ifaceName = ((EndpointSub) src).getIface();
        Pair<CompUse, String> key = new Pair<CompUse, String>(srcComp, ifaceName);
        List<Endpoint> set = callback.get(key);
        if (set == null) {
          set = new ArrayList<Endpoint>();
        }
        set.add(con.getEndpoint(Direction.out));
        callback.put(key, set);
      }
    }

    for (FuncIface src : input.keySet()) {
      List<Endpoint> conset = input.get(src);
      ArrayList<String> ns = new ArrayList<String>();
      FunctionBase func = genFunctions(conset, src, FuncInputHandlerEvent.class, FuncInputHandlerQuery.class);
      elem.addFunction(ns, func);
    }

    for (Pair<CompUse, String> src : callback.keySet()) {
      List<Endpoint> conset = callback.get(src);
      ArrayList<String> ns = new ArrayList<String>();
      ns.add(src.first.getName());
      FuncIfaceOut iface = getIface(src);
      FunctionBase func = genFunctions(conset, iface, FuncSubHandlerEvent.class, FuncSubHandlerQuery.class);
      elem.addFunction(ns, func);
    }

    map.put(obj, elem);

    return elem;
  }

  private Reference makeEntryExitFunc(String name, ListOfNamed<FunctionHeader> listOfNamed) {
    FuncPrivateVoid func = new FuncPrivateVoid(info, name, new ListOfNamed<FuncVariable>());
    func.setBody(new Block(info));
    listOfNamed.add(func);
    return new Reference(info, func);
  }

  public FunctionBase genFunctions(List<Endpoint> conset, FuncIface iface, Class<? extends FunctionBase> kindVoid, Class<? extends FunctionBase> kindRet) {
    FunctionBase ptoto = Copy.copy((FunctionBase) iface);

    Class<? extends FunctionBase> kind = iface instanceof FuncWithReturn ? kindRet : kindVoid;
    FunctionBase impl = FunctionFactory.create(kind, info, ptoto.getName(), ptoto.getParam());
    if (ptoto instanceof FuncWithReturn) {
      ((FuncWithReturn) impl).setRet(((FuncWithReturn) ptoto).getRet());
    }

    Block body = new Block(info);
    ((FuncWithBody) impl).setBody(body);

    if (ptoto instanceof FuncWithReturn) {
      assert (conset.size() == 1); // typechecker should find this
      Endpoint con = conset.get(0);
      ReturnExpr call = makeQueryCall(con, (FuncWithReturn) impl);
      body.getStatements().add(call);
    } else {
      for (Endpoint con : conset) {
        CallStmt call = makeEventCall(con, impl);
        body.getStatements().add(call);
      }
    }

    return impl;
  }

  static private ReturnExpr makeQueryCall(Endpoint ep, FuncWithReturn func) {
    Reference ref = epToRef(ep);

    List<Expression> actparam = new ArrayList<Expression>();
    for (Variable var : func.getParam()) {
      actparam.add(new Reference(func.getInfo(), var));
    }

    RefCall call = new RefCall(func.getInfo(), actparam);
    ref.getOffset().add(call);

    return new ReturnExpr(info, ref);
  }

  static private CallStmt makeEventCall(Endpoint ep, FunctionBase func) {
    assert (!(func instanceof FuncWithReturn));

    Reference ref = epToRef(ep);

    List<Expression> actparam = new ArrayList<Expression>();
    for (Variable var : func.getParam()) {
      actparam.add(new Reference(func.getInfo(), var));
    }

    RefCall call = new RefCall(func.getInfo(), actparam);
    ref.getOffset().add(call);

    return new CallStmt(func.getInfo(), ref);
  }

  static private Reference epToRef(Endpoint ep) {
    if (ep instanceof EndpointSelf) {
      return new Reference(ep.getInfo(), ((EndpointSelf) ep).getIface());
    } else {
      EndpointSub eps = (EndpointSub) ep;
      Reference ref = new Reference(eps.getInfo(), eps.getComp());
      ref.getOffset().add(new RefName(eps.getInfo(), eps.getIface()));
      return ref;
    }
  }

  private FuncIfaceOut getIface(Pair<CompUse, String> src) {
    Component comp = src.first.getLink();
    FuncIfaceOut iface = comp.getOutput().find(src.second);
    assert (iface != null);
    return iface;
  }
}
