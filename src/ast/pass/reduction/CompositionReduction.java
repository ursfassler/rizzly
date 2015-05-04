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

import util.Pair;
import ast.ElementInfo;
import ast.copy.Copy;
import ast.copy.Relinker;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.component.composition.AsynchroniusConnection;
import ast.data.component.composition.CompUse;
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
import ast.data.expression.TupleValue;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.function.Function;
import ast.data.function.InterfaceFunction;
import ast.data.function.header.FuncProcedure;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSignal;
import ast.data.function.header.FuncSlot;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.FuncSubHandlerQuery;
import ast.data.function.ret.FuncReturnNone;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.MsgPush;
import ast.data.statement.ReturnExpr;
import ast.data.variable.FuncVariable;
import ast.data.variable.Variable;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.NameFilter;
import ast.specification.AndSpec;
import ast.specification.IsClass;
import ast.specification.Specification;
import ast.traverser.NullTraverser;
import error.ErrorType;
import error.RError;

public class CompositionReduction extends AstPass {

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
    if (out instanceof FuncSignal) {
      func = new FuncSubHandlerEvent(out.getInfo(), out.name, Copy.copy(out.param), Copy.copy(out.ret), new Block(ElementInfo.NO));
    } else if (out instanceof FuncQuery) {
      func = new FuncSubHandlerQuery(out.getInfo(), out.name, Copy.copy(out.param), Copy.copy(out.ret), new Block(ElementInfo.NO));
    } else {
      throw new RuntimeException("not yet implemented");
    }
    return func;
  }

}

// TODO cleanup
class CompositionReductionWorker extends NullTraverser<Ast, Void> {

  private static final ElementInfo info = ElementInfo.NO;
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
    ElementInfo info = obj.getInfo();

    FuncProcedure entry = new FuncProcedure(info, "_entry", new AstList<FuncVariable>(), new FuncReturnNone(info), new Block(info));
    FuncProcedure exit = new FuncProcedure(info, "_exit", new AstList<FuncVariable>(), new FuncReturnNone(info), new Block(info));

    ImplElementary elem = new ImplElementary(obj.getInfo(), obj.name, new SimpleRef<FuncProcedure>(info, entry), new SimpleRef<FuncProcedure>(info, exit));
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
      Component usedComp = (Component) compu.compRef.getTarget();
      for (InterfaceFunction out : usedComp.getIface(Direction.out)) {
        Function suha = CompositionReduction.makeHandler(out);
        suc.func.add(suha);
        coca.put(new Pair<CompUse, Function>(compu, out), suha);
      }
    }

    for (Connection con : obj.connection) {
      Endpoint src = con.endpoint.get(Direction.in);

      if (src instanceof EndpointSelf) {
        Function coniface = src.getFunc();

        if (coniface instanceof FuncResponse) {
          // assert (elem.getResponse().contains(coniface));
          assert (coniface.body.statements.isEmpty());
          assert (con instanceof SynchroniusConnection);

          ReturnExpr call = makeQueryCall(con.endpoint.get(Direction.out), coniface);
          coniface.body.statements.add(call);
        } else if (coniface instanceof FuncSlot) {
          // assert (elem.getSlot().contains(coniface));
          genSlotCall(elem, con, coniface);
        } else {
          RError.err(ErrorType.Fatal, coniface.getInfo(), "Unexpected function type: " + coniface.getClass().getSimpleName());
        }
      } else {
        CompUse srcCompRef = ((EndpointSub) src).component.link;
        Component srcComp = (Component) srcCompRef.compRef.getTarget();
        InterfaceFunction funa = NameFilter.select(srcComp.iface, ((EndpointSub) src).function);
        Function coniface = funa;

        Function suha = coca.get(new Pair<CompUse, Function>(srcCompRef, coniface));

        if (coniface instanceof FuncQuery) {
          // assert (srcComp.getLink().getQuery().contains(coniface));
          assert (suha.body.statements.isEmpty());
          assert (con instanceof SynchroniusConnection);

          ReturnExpr call = makeQueryCall(con.endpoint.get(Direction.out), suha);
          suha.body.statements.add(call);
        } else if (coniface instanceof FuncSignal) {
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
    if (con instanceof SynchroniusConnection) {
      coniface.body.statements.add(makeEventCall(con.endpoint.get(Direction.out), coniface));
    } else if (con instanceof AsynchroniusConnection) {
      coniface.body.statements.add(makePostQueueCall(con.endpoint.get(Direction.out), coniface, elem));
    } else {
      RError.err(ErrorType.Error, con.getInfo(), "Unknown connection type: " + con.getClass().getCanonicalName());
    }
  }

  static private ReturnExpr makeQueryCall(Endpoint ep, Function func) {
    Reference ref = epToRef(ep);

    TupleValue actparam = new TupleValue(info, new AstList<Expression>());
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

    TupleValue actparam = new TupleValue(info, new AstList<Expression>());
    for (Variable var : func.param) {
      actparam.value.add(new Reference(func.getInfo(), var));
    }

    RefCall call = new RefCall(func.getInfo(), actparam);
    ref.offset.add(call);

    return new CallStmt(func.getInfo(), ref);
  }

  static private Reference epToRef(Endpoint ep) {
    if (ep instanceof EndpointSelf) {
      return new Reference(ep.getInfo(), ((EndpointSelf) ep).getFunc());
    } else {
      EndpointSub eps = (EndpointSub) ep;
      Reference ref = new Reference(eps.getInfo(), eps.component.link);
      ref.offset.add(new RefName(eps.getInfo(), eps.function));
      return ref;
    }
  }

  private Reference getQueue(Endpoint ep, Component comp) {
    Reference ref;
    if (ep instanceof EndpointSub) {
      ref = new Reference(ep.getInfo(), ((EndpointSub) ep).component.link);
      Component refComp = (Component) ((EndpointSub) ep).component.link.compRef.getTarget();
      Queue queue = refComp.queue;
      ref.offset.add(new RefName(info, queue.name));
    } else {
      ref = new Reference(ep.getInfo(), comp.queue);
    }
    return ref;
  }

  public Map<ImplComposition, ImplElementary> getMap() {
    return map;
  }

}
