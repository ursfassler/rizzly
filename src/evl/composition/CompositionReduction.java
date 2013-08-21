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
import evl.cfg.BasicBlockList;
import evl.cfg.ReturnExpr;
import evl.copy.Copy;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.function.FunctionFactory;
import evl.function.impl.FuncInputHandlerEvent;
import evl.function.impl.FuncInputHandlerQuery;
import evl.function.impl.FuncPrivateVoid;
import evl.function.impl.FuncSubHandlerEvent;
import evl.function.impl.FuncSubHandlerQuery;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.IfaceUse;
import evl.other.ImplElementary;
import evl.other.Interface;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.other.Namespace;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.variable.FuncVariable;
import evl.variable.Variable;
import fun.hfsm.State;

public class CompositionReduction extends NullTraverser<Named, Void> {
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
    elem.getIface(Direction.in).addAll(obj.getIface(Direction.in));
    elem.getIface(Direction.out).addAll(obj.getIface(Direction.out));

    elem.setEntryFunc(makeEntryExitFunc(State.ENTRY_FUNC_NAME, elem.getInternalFunction()));
    elem.setExitFunc(makeEntryExitFunc(State.EXIT_FUNC_NAME, elem.getInternalFunction()));

    Map<IfaceUse, List<Endpoint>> input = new HashMap<IfaceUse, List<Endpoint>>();
    Map<Pair<CompUse, String>, List<Endpoint>> callback = new HashMap<Pair<CompUse, String>, List<Endpoint>>();
    for (Connection con : obj.getConnection()) {
      assert (con.getType() == MessageType.sync);
      Endpoint src = con.getEndpoint(Direction.in);

      if (src instanceof EndpointSelf) {
        IfaceUse srcIface = ((EndpointSelf) src).getIface();
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

    for (IfaceUse src : input.keySet()) {
      List<Endpoint> conset = input.get(src);
      ArrayList<String> ns = new ArrayList<String>();
      ns.add(src.getName());
      Interface iface = src.getLink();
      List<FunctionBase> functions = genFunctions(conset, iface.getPrototype(), FuncInputHandlerEvent.class, FuncInputHandlerQuery.class);
      for (FunctionBase func : functions) {
        elem.addFunction(ns, func);
      }
    }

    for (Pair<CompUse, String> src : callback.keySet()) {
      List<Endpoint> conset = callback.get(src);
      ArrayList<String> ns = new ArrayList<String>();
      ns.add(src.first.getName());
      ns.add(src.second);
      Interface iface = getIface(src);
      List<FunctionBase> functions = genFunctions(conset, iface.getPrototype(), FuncSubHandlerEvent.class, FuncSubHandlerQuery.class);
      for (FunctionBase func : functions) {
        elem.addFunction(ns, func);
      }
    }

    map.put(obj, elem);

    return elem;
  }

  private Reference makeEntryExitFunc(String name, ListOfNamed<FunctionBase> list) {
    ElementInfo info = new ElementInfo();
    FuncPrivateVoid func = new FuncPrivateVoid(info, name, new ListOfNamed<Variable>());
    func.setBody(new BasicBlockList(info));
    list.add(func);
    return new Reference(info, func);
  }

  public List<FunctionBase> genFunctions(List<Endpoint> conset, ListOfNamed<? extends FunctionBase> functions, Class<? extends FunctionBase> kindVoid, Class<? extends FunctionBase> kindRet) {
    List<FunctionBase> ret = new ArrayList<FunctionBase>();
    for (FunctionBase fh : functions) {
      FunctionBase ptoto = Copy.copy(fh);

      Class<? extends FunctionBase> kind = fh instanceof FuncWithReturn ? kindRet : kindVoid;
      FunctionBase impl = FunctionFactory.create(kind, new ElementInfo(), ptoto.getName(), ptoto.getParam());
      if (ptoto instanceof FuncWithReturn) {
        ((FuncWithReturn) impl).setRet(((FuncWithReturn) ptoto).getRet());
      }

      BasicBlockList body = new BasicBlockList(new ElementInfo());
      ((FuncWithBody) impl).setBody(body);

      for (Endpoint con : conset) {
        Statement call = makeCall(con, impl);
        body.getStatements().add(call);
        if (call instanceof ReturnExpr) {
          assert (conset.size() == 1); // FIXME typechecker should find this
        }
      }

      ret.add(impl);
    }
    return ret;
  }

  private Statement makeCall(Endpoint ep, FunctionBase func) {
    Reference ref = epToRef(ep);
    ref.getOffset().add(new RefName(ep.getInfo(), func.getName()));

    List<Expression> actparam = new ArrayList<Expression>();
    for (FuncVariable var : func.getParam()) {
      actparam.add(new Reference(func.getInfo(), var));
    }

    RefCall call = new RefCall(func.getInfo(), actparam);
    ref.getOffset().add(call);

    if (func instanceof FuncWithReturn) {
      return new ReturnExpr(func.getInfo(), ref);
    } else {
      return new CallStmt(func.getInfo(), ref);
    }
  }

  private Reference epToRef(Endpoint ep) {
    if (ep instanceof EndpointSelf) {
      return new Reference(ep.getInfo(), ((EndpointSelf) ep).getIface());
    } else {
      EndpointSub eps = (EndpointSub) ep;
      Reference ref = new Reference(eps.getInfo(), eps.getComp());
      ref.getOffset().add(new RefName(eps.getInfo(), eps.getIface()));
      return ref;
    }
  }

  private Interface getIface(Pair<CompUse, String> src) {
    Component comp = src.first.getLink();
    IfaceUse iface = comp.getIface(Direction.out).find(src.second);
    assert (iface != null);
    return iface.getLink();
  }

}
