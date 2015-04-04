/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package evl.pass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pass.EvlPass;
import pass.NoItem;
import util.Pair;

import common.Direction;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.copy.Copy;
import evl.copy.Relinker;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.component.Component;
import evl.data.component.composition.CompUse;
import evl.data.component.composition.Connection;
import evl.data.component.composition.Endpoint;
import evl.data.component.composition.EndpointSelf;
import evl.data.component.composition.EndpointSub;
import evl.data.component.composition.ImplComposition;
import evl.data.component.composition.MessageType;
import evl.data.component.composition.Queue;
import evl.data.component.composition.SubCallbacks;
import evl.data.component.elementary.ImplElementary;
import evl.data.expression.Expression;
import evl.data.expression.TupleValue;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.Function;
import evl.data.function.InterfaceFunction;
import evl.data.function.header.FuncCtrlInDataIn;
import evl.data.function.header.FuncCtrlInDataOut;
import evl.data.function.header.FuncCtrlOutDataIn;
import evl.data.function.header.FuncCtrlOutDataOut;
import evl.data.function.header.FuncPrivateVoid;
import evl.data.function.header.FuncSubHandlerEvent;
import evl.data.function.header.FuncSubHandlerQuery;
import evl.data.function.ret.FuncReturnNone;
import evl.data.statement.Block;
import evl.data.statement.CallStmt;
import evl.data.statement.ReturnExpr;
import evl.data.statement.intern.MsgPush;
import evl.data.variable.FuncVariable;
import evl.data.variable.Variable;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;

public class CompositionReduction extends EvlPass {
  public CompositionReduction() {
    postcondition.add(new NoItem(Connection.class));
    postcondition.add(new NoItem(EndpointSelf.class));
    postcondition.add(new NoItem(EndpointSub.class));
    postcondition.add(new NoItem(ImplComposition.class));
  }

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    CompositionReductionWorker reduction = new CompositionReductionWorker(kb);
    reduction.traverse(evl, null);
    Relinker.relink(evl, reduction.getMap());

    // TODO reimplement
    // if (map.containsKey(root)) {
    // root = map.get(root);
    // }
  }

  public static Function makeHandler(Function out) {
    assert (out.body.statements.isEmpty());

    Function func;
    if (out instanceof FuncCtrlOutDataOut) {
      func = new FuncSubHandlerEvent(out.getInfo(), out.getName(), Copy.copy(out.param), Copy.copy(out.ret), new Block(ElementInfo.NO));
    } else if (out instanceof FuncCtrlOutDataIn) {
      func = new FuncSubHandlerQuery(out.getInfo(), out.getName(), Copy.copy(out.param), Copy.copy(out.ret), new Block(ElementInfo.NO));
    } else {
      throw new RuntimeException("not yet implemented");
    }
    return func;
  }

}

// TODO cleanup
class CompositionReductionWorker extends NullTraverser<Evl, Void> {

  private static final ElementInfo info = ElementInfo.NO;
  private final Map<ImplComposition, ImplElementary> map = new HashMap<ImplComposition, ImplElementary>();
  private final KnowBaseItem kbi;

  public CompositionReductionWorker(KnowledgeBase kb) {
    super();
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  @Override
  protected Evl visitDefault(Evl obj, Void param) {
    return obj;
  }

  @Override
  protected Evl visitNamespace(Namespace obj, Void param) {
    for (int i = 0; i < obj.getChildren().size(); i++) {
      Evl item = obj.getChildren().get(i);
      Map<Object, Object> prop = item.properties();
      item = visit(item, null);
      assert (item != null);
      item.properties().putAll(prop);
      obj.getChildren().set(i, item);
    }
    return obj;
  }

  @Override
  protected Evl visitImplComposition(ImplComposition obj, Void param) {
    ElementInfo info = obj.getInfo();

    FuncPrivateVoid entry = new FuncPrivateVoid(info, "_entry", new EvlList<FuncVariable>(), new FuncReturnNone(info), new Block(info));
    FuncPrivateVoid exit = new FuncPrivateVoid(info, "_exit", new EvlList<FuncVariable>(), new FuncReturnNone(info), new Block(info));

    ImplElementary elem = new ImplElementary(obj.getInfo(), obj.getName(), new SimpleRef<FuncPrivateVoid>(info, entry), new SimpleRef<FuncPrivateVoid>(info, exit));
    elem.function.add(entry);
    elem.function.add(exit);

    elem.component.addAll(obj.component);
    elem.iface.addAll(obj.iface);
    elem.function.addAll(obj.function);
    elem.queue = obj.queue;

    Map<Pair<CompUse, Function>, Function> coca = new HashMap<Pair<CompUse, Function>, Function>();

    for (CompUse compu : obj.component) {
      SubCallbacks suc = new SubCallbacks(compu.getInfo(), new SimpleRef<CompUse>(info, compu));
      elem.subCallback.add(suc);
      for (InterfaceFunction out : compu.link.getIface(Direction.out)) {
        Function suha = CompositionReduction.makeHandler((Function) out);
        suc.func.add(suha);
        coca.put(new Pair<CompUse, Function>(compu, (Function) out), suha);
      }
    }

    for (Connection con : obj.connection) {
      Endpoint src = con.endpoint.get(Direction.in);

      if (src instanceof EndpointSelf) {
        Function coniface = ((EndpointSelf) src).link;

        if (coniface instanceof FuncCtrlInDataOut) {
          // assert (elem.getResponse().contains(coniface));
          assert (coniface.body.statements.isEmpty());
          assert (con.type == MessageType.sync);

          ReturnExpr call = makeQueryCall(con.endpoint.get(Direction.out), coniface);
          coniface.body.statements.add(call);
        } else if (coniface instanceof FuncCtrlInDataIn) {
          // assert (elem.getSlot().contains(coniface));
          genSlotCall(elem, con, coniface);
        } else {
          RError.err(ErrorType.Fatal, coniface.getInfo(), "Unexpected function type: " + coniface.getClass().getSimpleName());
        }
      } else {
        CompUse srcComp = ((EndpointSub) src).link;
        InterfaceFunction funa = srcComp.link.iface.find(((EndpointSub) src).function);
        Function coniface = (Function) funa;

        Function suha = coca.get(new Pair<CompUse, Function>(srcComp, coniface));

        if (coniface instanceof FuncCtrlOutDataIn) {
          // assert (srcComp.getLink().getQuery().contains(coniface));
          assert (suha.body.statements.isEmpty());
          assert (con.type == MessageType.sync);

          ReturnExpr call = makeQueryCall(con.endpoint.get(Direction.out), suha);
          suha.body.statements.add(call);
        } else if (coniface instanceof FuncCtrlOutDataOut) {
          genSlotCall(elem, con, suha);
        } else {
          RError.err(ErrorType.Fatal, coniface.getInfo(), "Unexpected function type: " + coniface.getClass().getSimpleName());
        }
      }
    }

    getMap().put(obj, elem);
    return elem;
  }

  private void genSlotCall(ImplElementary elem, Connection con, Function coniface) {
    switch (con.type) {
      case sync:
        coniface.body.statements.add(makeEventCall(con.endpoint.get(Direction.out), coniface));
        break;
      case async:
        coniface.body.statements.add(makePostQueueCall(con.endpoint.get(Direction.out), coniface, elem));
        break;
      default:
        RError.err(ErrorType.Error, con.getInfo(), "Unknown connection type: " + con.type);
        return;
    }
  }

  static private ReturnExpr makeQueryCall(Endpoint ep, Function func) {
    Reference ref = epToRef(ep);

    TupleValue actparam = new TupleValue(info, new EvlList<Expression>());
    for (Variable var : func.param) {
      actparam.value.add(new Reference(func.getInfo(), var));
    }

    RefCall call = new RefCall(func.getInfo(), actparam);
    ref.offset.add(call);

    return new ReturnExpr(info, ref);
  }

  private MsgPush makePostQueueCall(Endpoint ep, Function func, Component comp) {
    Reference ref = epToRef(ep);

    List<Expression> actparam = new ArrayList<Expression>();
    for (Variable var : func.param) {
      actparam.add(new Reference(func.getInfo(), var));
    }

    Reference queue = getQueue(ep, comp);
    return new MsgPush(func.getInfo(), queue, ref, actparam);
  }

  static private CallStmt makeEventCall(Endpoint ep, Function func) {

    Reference ref = epToRef(ep);

    TupleValue actparam = new TupleValue(info, new EvlList<Expression>());
    for (Variable var : func.param) {
      actparam.value.add(new Reference(func.getInfo(), var));
    }

    RefCall call = new RefCall(func.getInfo(), actparam);
    ref.offset.add(call);

    return new CallStmt(func.getInfo(), ref);
  }

  static private Reference epToRef(Endpoint ep) {
    if (ep instanceof EndpointSelf) {
      return new Reference(ep.getInfo(), ((EndpointSelf) ep).link);
    } else {
      EndpointSub eps = (EndpointSub) ep;
      Reference ref = new Reference(eps.getInfo(), eps.link);
      ref.offset.add(new RefName(eps.getInfo(), eps.function));
      return ref;
    }
  }

  private Reference getQueue(Endpoint ep, Component comp) {
    Reference ref;
    if (ep instanceof EndpointSub) {
      ref = new Reference(ep.getInfo(), ((EndpointSub) ep).link);
      Queue queue = ((EndpointSub) ep).link.link.queue;
      ref.offset.add(new RefName(info, queue.getName()));
    } else {
      ref = new Reference(ep.getInfo(), comp.queue);
    }
    return ref;
  }

  public Map<ImplComposition, ImplElementary> getMap() {
    return map;
  }

}
