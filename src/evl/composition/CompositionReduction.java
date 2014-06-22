package evl.composition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Pair;

import common.Direction;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
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
import evl.function.impl.FuncImplResponse;
import evl.function.impl.FuncImplSlot;
import evl.function.impl.FuncPrivateVoid;
import evl.function.impl.FuncSubHandlerEvent;
import evl.function.impl.FuncSubHandlerQuery;
import evl.knowledge.KnowParent;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.ImplElementary;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.other.Namespace;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.ReturnExpr;
import evl.statement.intern.MsgPush;
import evl.variable.FuncVariable;
import evl.variable.Variable;
import fun.hfsm.State;

//TODO cleanup
public class CompositionReduction extends NullTraverser<Named, Void> {

  private static final ElementInfo info = new ElementInfo();
  private Map<ImplComposition, ImplElementary> map = new HashMap<ImplComposition, ImplElementary>();
  private final KnowParent kp;

  public CompositionReduction(KnowledgeBase kb) {
    super();
    kp = kb.getEntry(KnowParent.class);
  }

  public static Map<ImplComposition, ImplElementary> process(Namespace classes, KnowledgeBase kb) {
    CompositionReduction reduction = new CompositionReduction(kb);
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
    elem.getSignal().addAll(obj.getSignal());
    elem.getQuery().addAll(obj.getQuery());
    elem.getSlot().addAll(obj.getSlot());
    elem.getResponse().addAll(obj.getResponse());
    elem.setQueue(obj.getQueue());

    elem.setEntryFunc(makeEntryExitFunc(State.ENTRY_FUNC_NAME, elem.getFunction()));
    elem.setExitFunc(makeEntryExitFunc(State.EXIT_FUNC_NAME, elem.getFunction()));

    Map<FuncIface, List<Connection>> input = new HashMap<FuncIface, List<Connection>>();
    Map<Pair<CompUse, String>, List<Connection>> callback = new HashMap<Pair<CompUse, String>, List<Connection>>();
    for (Connection con : obj.getConnection()) {
      Endpoint src = con.getEndpoint(Direction.in);

      if (src instanceof EndpointSelf) {
        FuncIface srcIface = ((EndpointSelf) src).getIface();
        List<Connection> set = input.get(srcIface);
        if (set == null) {
          set = new ArrayList<Connection>();
        }
        set.add(con);
        input.put(srcIface, set);
      } else {
        CompUse srcComp = ((EndpointSub) src).getComp();
        String ifaceName = ((EndpointSub) src).getIface();
        Pair<CompUse, String> key = new Pair<CompUse, String>(srcComp, ifaceName);
        List<Connection> set = callback.get(key);
        if (set == null) {
          set = new ArrayList<Connection>();
        }
        set.add(con);
        callback.put(key, set);
      }
    }

    for (FuncIface src : input.keySet()) {
      List<Connection> conset = input.get(src);
      FunctionBase func;
      if (src instanceof FuncWithReturn) {
        func = genResponse(conset, (FuncWithReturn) src, obj, FuncImplResponse.class);
      } else {
        func = genSlot(conset, src, obj, FuncImplSlot.class);
      }
      elem.getFunction().add(func);
    }

    for (Pair<CompUse, String> src : callback.keySet()) {
      List<Connection> conset = callback.get(src);
      FuncIfaceOut iface = getIface(src);
      FunctionHeader func;
      if (iface instanceof FuncWithReturn) {
        func = genResponse(conset, (FuncWithReturn) iface, obj, FuncSubHandlerQuery.class);
      } else {
        func = genSlot(conset, iface, obj, FuncSubHandlerEvent.class);
      }
      elem.addSubCallback(src.first.getName(), func);
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

  public FunctionBase genSlot(List<Connection> conset, FuncIface iface, Component comp, Class<? extends FunctionBase> kind) {
    FunctionBase ptoto = Copy.copy((FunctionBase) iface);

    FunctionBase impl = FunctionFactory.create(kind, info, iface.getName(), ptoto.getParam());

    Block body = new Block(info);
    ((FuncWithBody) impl).setBody(body);

    for (Connection con : conset) {
      switch (con.getType()) {
      case sync:
        body.getStatements().add(makeEventCall(con.getEndpoint(Direction.out), impl));
        break;
      case async:
        body.getStatements().add(makePostQueueCall(con.getEndpoint(Direction.out), impl, comp));
        break;
      default:
        RError.err(ErrorType.Error, con.getInfo(), "Unknown connection type: " + con.getType());
        continue;
      }
    }

    return impl;
  }

  public FunctionBase genResponse(List<Connection> conset, FuncWithReturn iface, Component comp, Class<? extends FunctionBase> kind) {
    FunctionBase ptoto = Copy.copy((FunctionBase) iface);

    FunctionBase impl = FunctionFactory.create(kind, info, iface.getName(), ptoto.getParam());
    ((FuncWithReturn) impl).setRet(((FuncWithReturn) ptoto).getRet());

    Block body = new Block(info);
    ((FuncWithBody) impl).setBody(body);

    assert (conset.size() == 1); // typechecker should find this
    assert (conset.get(0).getType() == MessageType.sync); // typechecker should find this
    Connection con = conset.get(0);
    ReturnExpr call = makeQueryCall(con.getEndpoint(Direction.out), (FuncWithReturn) impl);
    body.getStatements().add(call);

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

  private MsgPush makePostQueueCall(Endpoint ep, FunctionBase func, Component comp) {
    assert (!(func instanceof FuncWithReturn));

    Reference ref = epToRef(ep);

    List<Expression> actparam = new ArrayList<Expression>();
    for (Variable var : func.getParam()) {
      actparam.add(new Reference(func.getInfo(), var));
    }

    Reference queue = getQueue(ep, comp);
    return new MsgPush(func.getInfo(), queue, ref, actparam);
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

  private Reference getQueue(Endpoint ep, Component comp) {
    Reference ref;
    if (ep instanceof EndpointSub) {
      ref = new Reference(ep.getInfo(), ((EndpointSub) ep).getComp());
      String qname = ((EndpointSub) ep).getComp().getLink().getQueue().getName();
      ref.getOffset().add(new RefName(info, qname));
    } else {
      ref = new Reference(ep.getInfo(), comp.getQueue());
    }
    return ref;
  }

  private FuncIfaceOut getIface(Pair<CompUse, String> src) {
    Component comp = src.first.getLink();
    FuncIfaceOut iface = (FuncIfaceOut) comp.getIface(Direction.out).find(src.second);
    assert (iface != null);
    return iface;
  }
}
