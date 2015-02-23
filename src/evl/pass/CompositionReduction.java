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
import util.Pair;

import common.Direction;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.composition.Connection;
import evl.composition.Endpoint;
import evl.composition.EndpointSelf;
import evl.composition.EndpointSub;
import evl.composition.ImplComposition;
import evl.composition.MessageType;
import evl.copy.Copy;
import evl.copy.Relinker;
import evl.expression.Expression;
import evl.expression.TupleValue;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.function.InterfaceFunction;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataIn;
import evl.function.header.FuncCtrlOutDataOut;
import evl.function.header.FuncPrivateVoid;
import evl.function.header.FuncSubHandlerEvent;
import evl.function.header.FuncSubHandlerQuery;
import evl.function.ret.FuncReturnNone;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.EvlList;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.other.Queue;
import evl.other.SubCallbacks;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.ReturnExpr;
import evl.statement.intern.MsgPush;
import evl.variable.FuncVariable;
import evl.variable.Variable;

public class CompositionReduction extends EvlPass {

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
    assert (out.getBody().getStatements().isEmpty());

    Function func;
    if (out instanceof FuncCtrlOutDataOut) {
      func = new FuncSubHandlerEvent(ElementInfo.NO, out.getName(), Copy.copy(out.getParam()), Copy.copy(out.getRet()), new Block(ElementInfo.NO));
    } else if (out instanceof FuncCtrlOutDataIn) {
      func = new FuncSubHandlerQuery(ElementInfo.NO, out.getName(), Copy.copy(out.getParam()), Copy.copy(out.getRet()), new Block(ElementInfo.NO));
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

    FuncPrivateVoid entry = new FuncPrivateVoid(info, "_entry", new EvlList<FuncVariable>(), new FuncReturnNone(info), new Block(info));
    FuncPrivateVoid exit = new FuncPrivateVoid(info, "_exit", new EvlList<FuncVariable>(), new FuncReturnNone(info), new Block(info));

    ImplElementary elem = new ImplElementary(obj.getInfo(), obj.getName(), new SimpleRef<FuncPrivateVoid>(info, entry), new SimpleRef<FuncPrivateVoid>(info, exit));
    elem.getFunction().add(entry);
    elem.getFunction().add(exit);

    elem.getComponent().addAll(obj.getComponent());
    elem.getIface().addAll(obj.getIface());
    elem.getFunction().addAll(obj.getFunction());
    elem.setQueue(obj.getQueue());

    Map<Pair<CompUse, Function>, Function> coca = new HashMap<Pair<CompUse, Function>, Function>();

    for (CompUse compu : obj.getComponent()) {
      SubCallbacks suc = new SubCallbacks(compu.getInfo(), new SimpleRef<CompUse>(info, compu));
      elem.getSubCallback().add(suc);
      for (InterfaceFunction out : compu.getLink().getIface(Direction.out)) {
        Function suha = CompositionReduction.makeHandler((Function) out);
        suc.getFunc().add(suha);
        coca.put(new Pair<CompUse, Function>(compu, (Function) out), suha);
      }
    }

    for (Connection con : obj.getConnection()) {
      Endpoint src = con.getEndpoint(Direction.in);

      if (src instanceof EndpointSelf) {
        Function coniface = ((EndpointSelf) src).getLink();

        if (coniface instanceof FuncCtrlInDataOut) {
          // assert (elem.getResponse().contains(coniface));
          assert (coniface.getBody().getStatements().isEmpty());
          assert (con.getType() == MessageType.sync);

          ReturnExpr call = makeQueryCall(con.getEndpoint(Direction.out), coniface);
          coniface.getBody().getStatements().add(call);
        } else if (coniface instanceof FuncCtrlInDataIn) {
          // assert (elem.getSlot().contains(coniface));
          genSlotCall(elem, con, coniface);
        } else {
          RError.err(ErrorType.Fatal, coniface.getInfo(), "Unexpected function type: " + coniface.getClass().getSimpleName());
        }
      } else {
        CompUse srcComp = ((EndpointSub) src).getLink();
        InterfaceFunction funa = srcComp.getLink().getIface().find(((EndpointSub) src).getFunction());
        Function coniface = (Function) funa;

        Function suha = coca.get(new Pair<CompUse, Function>(srcComp, coniface));

        if (coniface instanceof FuncCtrlOutDataIn) {
          // assert (srcComp.getLink().getQuery().contains(coniface));
          assert (suha.getBody().getStatements().isEmpty());
          assert (con.getType() == MessageType.sync);

          ReturnExpr call = makeQueryCall(con.getEndpoint(Direction.out), suha);
          suha.getBody().getStatements().add(call);
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
    switch (con.getType()) {
      case sync:
        coniface.getBody().getStatements().add(makeEventCall(con.getEndpoint(Direction.out), coniface));
        break;
      case async:
        coniface.getBody().getStatements().add(makePostQueueCall(con.getEndpoint(Direction.out), coniface, elem));
        break;
      default:
        RError.err(ErrorType.Error, con.getInfo(), "Unknown connection type: " + con.getType());
        return;
    }
  }

  static private ReturnExpr makeQueryCall(Endpoint ep, Function func) {
    Reference ref = epToRef(ep);

    TupleValue actparam = new TupleValue(info, new EvlList<Expression>());
    for (Variable var : func.getParam()) {
      actparam.getValue().add(new Reference(func.getInfo(), var));
    }

    RefCall call = new RefCall(func.getInfo(), actparam);
    ref.getOffset().add(call);

    return new ReturnExpr(info, ref);
  }

  private MsgPush makePostQueueCall(Endpoint ep, Function func, Component comp) {
    Reference ref = epToRef(ep);

    List<Expression> actparam = new ArrayList<Expression>();
    for (Variable var : func.getParam()) {
      actparam.add(new Reference(func.getInfo(), var));
    }

    Reference queue = getQueue(ep, comp);
    return new MsgPush(func.getInfo(), queue, ref, actparam);
  }

  static private CallStmt makeEventCall(Endpoint ep, Function func) {

    Reference ref = epToRef(ep);

    TupleValue actparam = new TupleValue(info, new EvlList<Expression>());
    for (Variable var : func.getParam()) {
      actparam.getValue().add(new Reference(func.getInfo(), var));
    }

    RefCall call = new RefCall(func.getInfo(), actparam);
    ref.getOffset().add(call);

    return new CallStmt(func.getInfo(), ref);
  }

  static private Reference epToRef(Endpoint ep) {
    if (ep instanceof EndpointSelf) {
      return new Reference(ep.getInfo(), ((EndpointSelf) ep).getLink());
    } else {
      EndpointSub eps = (EndpointSub) ep;
      Reference ref = new Reference(eps.getInfo(), eps.getLink());
      ref.getOffset().add(new RefName(eps.getInfo(), eps.getFunction()));
      return ref;
    }
  }

  private Reference getQueue(Endpoint ep, Component comp) {
    Reference ref;
    if (ep instanceof EndpointSub) {
      ref = new Reference(ep.getInfo(), ((EndpointSub) ep).getLink());
      Queue queue = ((EndpointSub) ep).getLink().getLink().getQueue();
      ref.getOffset().add(new RefName(info, queue.getName()));
    } else {
      ref = new Reference(ep.getInfo(), comp.getQueue());
    }
    return ref;
  }

  public Map<ImplComposition, ImplElementary> getMap() {
    return map;
  }

}
