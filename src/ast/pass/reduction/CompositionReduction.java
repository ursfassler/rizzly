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

package ast.pass.reduction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Pair;
import ast.copy.Copy;
import ast.copy.Relinker;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.component.composition.AsynchroniusConnection;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.Connection;
import ast.data.component.composition.Direction;
import ast.data.component.composition.Endpoint;
import ast.data.component.composition.EndpointSelf;
import ast.data.component.composition.EndpointSub;
import ast.data.component.composition.ImplComposition;
import ast.data.component.composition.Queue;
import ast.data.component.composition.SubCallbacks;
import ast.data.component.composition.SynchroniusConnection;
import ast.data.component.elementary.ImplElementary;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.value.TupleValue;
import ast.data.function.FuncRefFactory;
import ast.data.function.Function;
import ast.data.function.InterfaceFunction;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.FuncSubHandlerQuery;
import ast.data.function.header.Procedure;
import ast.data.function.header.Response;
import ast.data.function.header.Signal;
import ast.data.function.header.Slot;
import ast.data.function.ret.FuncReturnNone;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefCall;
import ast.data.reference.RefFactory;
import ast.data.reference.RefItem;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.ExpressionReturn;
import ast.data.statement.MsgPush;
import ast.data.variable.FunctionVariable;
import ast.data.variable.Variable;
import ast.dispatcher.NullDispatcher;
import ast.knowledge.KnowledgeBase;
import ast.meta.MetaList;
import ast.pass.AstPass;
import ast.repository.query.EndpointFunctionQuery;
import ast.repository.query.NameFilter;
import ast.repository.query.Referencees.TargetResolver;
import ast.visitor.VisitExecutorImplementation;
import error.ErrorType;
import error.RError;

public class CompositionReduction implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    TargetResolver targetResolver = new TargetResolver();
    CompositionReductionWorker reduction = new CompositionReductionWorker(targetResolver);
    reduction.traverse(ast, null);
    Relinker.relink(ast, reduction.getMap());

    // TODO reimplement
    // if (map.containsKey(root)) {
    // root = map.get(root);
    // }
  }

  public static Function makeHandler(Function out) {
    assert (out.body.statements.isEmpty());

    Function func;
    if (out instanceof Signal) {
      func = new FuncSubHandlerEvent(out.getName(), Copy.copy(out.param), Copy.copy(out.ret), new Block());
    } else if (out instanceof FuncQuery) {
      func = new FuncSubHandlerQuery(out.getName(), Copy.copy(out.param), Copy.copy(out.ret), new Block());
    } else {
      throw new RuntimeException("not yet implemented");
    }
    func.metadata().add(out.metadata());
    return func;
  }

}

// TODO cleanup
class CompositionReductionWorker extends NullDispatcher<Ast, Void> {
  private final TargetResolver targetResolver;
  private final Map<ImplComposition, ImplElementary> map = new HashMap<ImplComposition, ImplElementary>();

  public CompositionReductionWorker(TargetResolver targetResolver) {
    super();
    this.targetResolver = targetResolver;
  }

  @Override
  protected Ast visitDefault(Ast obj, Void param) {
    return obj;
  }

  @Override
  protected Ast visitNamespace(Namespace obj, Void param) {
    for (int i = 0; i < obj.children.size(); i++) {
      Ast item = obj.children.get(i);
      item = visit(item, null);
      assert (item != null);
      obj.children.set(i, item);
    }
    return obj;
  }

  @Override
  protected Ast visitImplComposition(ImplComposition obj, Void param) {
    MetaList info = obj.metadata();  // TODO use this info for everything in this function

    Procedure entry = new Procedure("_entry", new AstList<FunctionVariable>(), new FuncReturnNone(), new Block());
    Procedure exit = new Procedure("_exit", new AstList<FunctionVariable>(), new FuncReturnNone(), new Block());

    ImplElementary elem = new ImplElementary(obj.getName(), FuncRefFactory.create(entry), FuncRefFactory.create(exit));
    elem.function.add(entry);
    elem.function.add(exit);

    elem.component.addAll(obj.component);
    elem.iface.addAll(obj.iface);
    elem.function.addAll(obj.function);
    elem.queue = obj.queue;

    Map<Pair<ComponentUse, Function>, Function> coca = new HashMap<Pair<ComponentUse, Function>, Function>();

    for (ComponentUse compu : obj.component) {
      SubCallbacks suc = new SubCallbacks(RefFactory.create(info, compu));
      suc.metadata().add(compu.metadata());
      elem.subCallback.add(suc);
      Component usedComp = targetResolver.targetOf(compu.getCompRef(), Component.class);
      for (InterfaceFunction out : usedComp.getIface(Direction.out)) {
        Function suha = CompositionReduction.makeHandler(out);
        suc.func.add(suha);
        coca.put(new Pair<ComponentUse, Function>(compu, out), suha);
      }
    }

    for (Connection con : obj.connection) {
      Endpoint src = con.getSrc();

      if (src instanceof EndpointSelf) {
        EndpointFunctionQuery query = new EndpointFunctionQuery(targetResolver);
        VisitExecutorImplementation.instance().visit(query, src);
        Function coniface = query.getFunction();

        if (coniface instanceof Response) {
          // assert (elem.getResponse().contains(coniface));
          assert (coniface.body.statements.isEmpty());
          assert (con instanceof SynchroniusConnection);

          ExpressionReturn call = makeQueryCall(con.getDst(), coniface);
          coniface.body.statements.add(call);
        } else if (coniface instanceof Slot) {
          // assert (elem.getSlot().contains(coniface));
          genSlotCall(elem, con, coniface);
        } else {
          RError.err(ErrorType.Fatal, "Unexpected function type: " + coniface.getClass().getSimpleName(), coniface.metadata());
        }
      } else {
        ComponentUse srcCompRef = targetResolver.targetOf(((EndpointSub) src).getComponent(), ComponentUse.class);
        Component srcComp = targetResolver.targetOf(srcCompRef.getCompRef(), Component.class);
        Function coniface = NameFilter.select(srcComp.iface, ((EndpointSub) src).getFunction());

        Function suha = coca.get(new Pair<ComponentUse, Function>(srcCompRef, coniface));

        if (coniface instanceof FuncQuery) {
          // assert (srcComp.getLink().getQuery().contains(coniface));
          assert (suha.body.statements.isEmpty());
          assert (con instanceof SynchroniusConnection);

          ExpressionReturn call = makeQueryCall(con.getDst(), suha);
          suha.body.statements.add(call);
        } else if (coniface instanceof Signal) {
          genSlotCall(elem, con, suha);
        } else {
          RError.err(ErrorType.Fatal, "Unexpected function type: " + coniface.getClass().getSimpleName(), coniface.metadata());
        }
      }
    }

    getMap().put(obj, elem);
    return elem;
  }

  private void genSlotCall(ImplElementary elem, Connection con, Function coniface) {
    if (con instanceof SynchroniusConnection) {
      coniface.body.statements.add(makeEventCall(con.getDst(), coniface));
    } else if (con instanceof AsynchroniusConnection) {
      coniface.body.statements.add(makePostQueueCall(con.getDst(), coniface, elem));
    } else {
      RError.err(ErrorType.Error, "Unknown connection type: " + con.getClass().getCanonicalName(), con.metadata());
    }
  }

  private ExpressionReturn makeQueryCall(Endpoint ep, Function func) {
    OffsetReference ref = oldEpToRef(ep);

    TupleValue actparam = new TupleValue(new AstList<Expression>());
    for (Variable var : func.param) {
      actparam.value.add(new ReferenceExpression(RefFactory.withOffset(func.metadata(), var)));
    }

    RefCall call = new RefCall(actparam);
    call.metadata().add(func.metadata());
    ref.getOffset().add(call);

    return new ExpressionReturn(new ReferenceExpression(ref));
  }

  private MsgPush makePostQueueCall(Endpoint ep, Function func, Component comp) {
    Reference ref = epToRef(ep);

    List<Expression> actparam = new ArrayList<Expression>();
    for (Variable var : func.param) {
      actparam.add(new ReferenceExpression(RefFactory.withOffset(func.metadata(), var)));
    }

    Reference queue = getQueue(ep, comp);
    MsgPush push = new MsgPush(queue, ref, actparam);
    push.metadata().add(func.metadata());
    return push;
  }

  private CallStmt makeEventCall(Endpoint ep, Function func) {
    OffsetReference ref = oldEpToRef(ep);

    TupleValue actparam = new TupleValue(new AstList<Expression>());
    for (Variable var : func.param) {
      actparam.value.add(new ReferenceExpression(RefFactory.withOffset(func.metadata(), var)));
    }

    RefCall call = new RefCall(actparam);
    call.metadata().add(func.metadata());
    ref.getOffset().add(call);

    CallStmt callStmt = new CallStmt(ref);
    callStmt.metadata().add(func.metadata());
    return callStmt;
  }

  private Reference epToRef(Endpoint ep) {
    EndpointFunctionQuery query = new EndpointFunctionQuery(targetResolver);
    VisitExecutorImplementation.instance().visit(query, ep);
    Function func = query.getFunction();

    if (ep instanceof EndpointSelf) {
      return RefFactory.create(ep.metadata(), func);
    } else {
      EndpointSub eps = (EndpointSub) ep;
      OffsetReference ref = RefFactory.create(eps.metadata(), targetResolver.targetOf(eps.getComponent(), Named.class), new AstList<RefItem>());
      ref.getOffset().add(new RefName(eps.metadata(), eps.getFunction()));
      return ref;
    }
  }

  private OffsetReference oldEpToRef(Endpoint ep) {
    if (ep instanceof EndpointSelf) {
      EndpointFunctionQuery query = new EndpointFunctionQuery(targetResolver);
      VisitExecutorImplementation.instance().visit(query, ep);
      Function func = query.getFunction();

      return RefFactory.withOffset(ep.metadata(), func);
    } else {
      EndpointSub eps = (EndpointSub) ep;
      OffsetReference ref = RefFactory.withOffset(eps.metadata(), targetResolver.targetOf(eps.getComponent(), Named.class));
      ref.getOffset().add(new RefName(eps.metadata(), eps.getFunction()));
      return ref;
    }
  }

  private Reference getQueue(Endpoint ep, Component comp) {
    OffsetReference ref;
    if (ep instanceof EndpointSub) {
      ComponentUse compUse = targetResolver.targetOf(((EndpointSub) ep).getComponent(), ComponentUse.class);
      ref = RefFactory.withOffset(ep.metadata(), compUse);
      Component refComp = targetResolver.targetOf(compUse.getCompRef(), Component.class);
      Queue queue = refComp.queue;
      ref.getOffset().add(new RefName(queue.getName()));
    } else {
      ref = RefFactory.withOffset(ep.metadata(), comp.queue);
    }
    return ref;
  }

  public Map<ImplComposition, ImplElementary> getMap() {
    return map;
  }

}
