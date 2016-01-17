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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import main.Configuration;
import util.Pair;
import ast.copy.Copy;
import ast.copy.Relinker;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.component.composition.AsynchroniusConnection;
import ast.data.component.composition.CompUse;
import ast.data.component.composition.CompUseRef;
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
import ast.data.function.FuncRef;
import ast.data.function.FuncRefFactory;
import ast.data.function.Function;
import ast.data.function.InterfaceFunction;
import ast.data.function.header.FuncProcedure;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.FuncSubHandlerQuery;
import ast.data.function.header.Signal;
import ast.data.function.header.Slot;
import ast.data.function.ret.FuncReturnNone;
import ast.data.reference.RefCall;
import ast.data.reference.RefFactory;
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
import ast.repository.query.NameFilter;
import ast.specification.AndSpec;
import ast.specification.IsClass;
import ast.specification.Specification;
import error.ErrorType;
import error.RError;

public class CompositionReduction extends AstPass {
  public CompositionReduction(Configuration configuration) {
    super(configuration);
  }

  @Override
  public Specification getPostcondition() {
    Collection<Specification> specset = new HashSet<Specification>();
    specset.add(new IsClass(Connection.class).not());
    specset.add(new IsClass(EndpointSelf.class).not());
    specset.add(new IsClass(EndpointSub.class).not());
    specset.add(new IsClass(ImplComposition.class).not());
    return new AndSpec(specset);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    CompositionReductionWorker reduction = new CompositionReductionWorker();
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

  private final Map<ImplComposition, ImplElementary> map = new HashMap<ImplComposition, ImplElementary>();

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

    FuncProcedure entry = new FuncProcedure("_entry", new AstList<FunctionVariable>(), new FuncReturnNone(), new Block());
    FuncProcedure exit = new FuncProcedure("_exit", new AstList<FunctionVariable>(), new FuncReturnNone(), new Block());

    ImplElementary elem = new ImplElementary(obj.getName(), FuncRefFactory.create(entry), FuncRefFactory.create(exit));
    elem.function.add(entry);
    elem.function.add(exit);

    elem.component.addAll(obj.component);
    elem.iface.addAll(obj.iface);
    elem.function.addAll(obj.function);
    elem.queue = obj.queue;

    Map<Pair<CompUse, Function>, Function> coca = new HashMap<Pair<CompUse, Function>, Function>();

    for (CompUse compu : obj.component) {
      SubCallbacks suc = new SubCallbacks(new CompUseRef(info, RefFactory.create(info, compu)));
      suc.metadata().add(compu.metadata());
      elem.subCallback.add(suc);
      Component usedComp = compu.compRef.getTarget();
      for (InterfaceFunction out : usedComp.getIface(Direction.out)) {
        Function suha = CompositionReduction.makeHandler(out);
        suc.func.add(suha);
        coca.put(new Pair<CompUse, Function>(compu, out), suha);
      }
    }

    for (Connection con : obj.connection) {
      Endpoint src = con.getSrc();

      if (src instanceof EndpointSelf) {
        Function coniface = src.getFunc();

        if (coniface instanceof FuncResponse) {
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
        CompUse srcCompRef = ((EndpointSub) src).component.getTarget();
        Component srcComp = srcCompRef.compRef.getTarget();
        InterfaceFunction funa = NameFilter.select(srcComp.iface, ((EndpointSub) src).function);
        Function coniface = funa;

        Function suha = coca.get(new Pair<CompUse, Function>(srcCompRef, coniface));

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

  static private ExpressionReturn makeQueryCall(Endpoint ep, Function func) {
    Reference ref = epToRef(ep);

    TupleValue actparam = new TupleValue(new AstList<Expression>());
    for (Variable var : func.param) {
      actparam.value.add(new ReferenceExpression(RefFactory.full(func.metadata(), var)));
    }

    RefCall call = new RefCall(actparam);
    call.metadata().add(func.metadata());
    ref.offset.add(call);

    return new ExpressionReturn(new ReferenceExpression(ref));
  }

  private MsgPush makePostQueueCall(Endpoint ep, Function func, Component comp) {
    Reference ref = epToRef(ep);

    List<Expression> actparam = new ArrayList<Expression>();
    for (Variable var : func.param) {
      actparam.add(new ReferenceExpression(RefFactory.full(func.metadata(), var)));
    }

    Reference queue = getQueue(ep, comp);
    MsgPush push = new MsgPush(queue, new FuncRef(ref), actparam);
    push.metadata().add(func.metadata());
    return push;
  }

  static private CallStmt makeEventCall(Endpoint ep, Function func) {

    Reference ref = epToRef(ep);

    TupleValue actparam = new TupleValue(new AstList<Expression>());
    for (Variable var : func.param) {
      actparam.value.add(new ReferenceExpression(RefFactory.full(func.metadata(), var)));
    }

    RefCall call = new RefCall(actparam);
    call.metadata().add(func.metadata());
    ref.offset.add(call);

    CallStmt callStmt = new CallStmt(ref);
    callStmt.metadata().add(func.metadata());
    return callStmt;
  }

  static private Reference epToRef(Endpoint ep) {
    if (ep instanceof EndpointSelf) {
      return RefFactory.full(ep.metadata(), ((EndpointSelf) ep).getFunc());
    } else {
      EndpointSub eps = (EndpointSub) ep;
      Reference ref = RefFactory.full(eps.metadata(), eps.component.getTarget());
      ref.offset.add(new RefName(eps.metadata(), eps.function));
      return ref;
    }
  }

  private Reference getQueue(Endpoint ep, Component comp) {
    Reference ref;
    if (ep instanceof EndpointSub) {
      ref = RefFactory.full(ep.metadata(), ((EndpointSub) ep).component.getTarget());
      Component refComp = ((EndpointSub) ep).component.getTarget().compRef.getTarget();
      Queue queue = refComp.queue;
      ref.offset.add(new RefName(queue.getName()));
    } else {
      ref = RefFactory.full(ep.metadata(), comp.queue);
    }
    return ref;
  }

  public Map<ImplComposition, ImplElementary> getMap() {
    return map;
  }

}
