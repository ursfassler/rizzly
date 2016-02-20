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

package ast.pass.reduction.hfsm;

import java.util.HashMap;
import java.util.Map;

import main.Configuration;
import ast.Designator;
import ast.copy.Copy;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateContent;
import ast.data.component.hfsm.StateSimple;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.Response;
import ast.data.reference.RefFactory;
import ast.data.reference.Reference;
import ast.data.statement.Block;
import ast.data.statement.ExpressionReturn;
import ast.data.variable.Variable;
import ast.dispatcher.NullDispatcher;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.Collector;
import ast.repository.query.TypeFilter;
import ast.specification.IsClass;

public class QueryDownPropagator extends AstPass {
  public QueryDownPropagator(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    for (Ast hfsm : Collector.select(ast, new IsClass(ImplHfsm.class))) {
      process((ImplHfsm) hfsm, kb);
    }
  }

  private static void process(ImplHfsm hfsm, KnowledgeBase kb) {
    Map<Response, FuncFunction> qfunc = new HashMap<Response, FuncFunction>();
    QueryFuncMaker qfmaker = new QueryFuncMaker(qfunc);
    qfmaker.traverse(hfsm.topstate, new Designator());

    for (FuncFunction func : qfunc.values()) {
      hfsm.topstate.item.add(func);
    }

    QueryDownPropagatorWorker redirecter = new QueryDownPropagatorWorker(qfunc);
    redirecter.traverse(hfsm.topstate, new QueryParam());
  }

}

class QueryDownPropagatorWorker extends NullDispatcher<Void, QueryParam> {
  private final Map<Response, FuncFunction> map; // TODO do we need

  // that?

  public QueryDownPropagatorWorker(Map<Response, FuncFunction> map) {
    this.map = map;
  }

  @Override
  protected Void visitDefault(Ast obj, QueryParam param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, QueryParam param) {
    AstList<Response> queryList = TypeFilter.select(obj.item, Response.class);
    obj.item.removeAll(queryList);

    AstList<Response> queries = new AstList<Response>();

    for (Response query : param.before) {
      addQuery(queries, query);
    }
    for (Response query : queryList) {
      addQuery(queries, query);
    }
    for (Response query : param.after) {
      addQuery(queries, query);
    }

    for (Response func : queries) {
      Response cfunc = new Response(func.getName(), Copy.copy(func.param), Copy.copy(func.ret), new Block());

      AstList<Expression> acpar = new AstList<Expression>();
      for (Variable par : cfunc.param) {
        acpar.add(new ReferenceExpression(RefFactory.withOffset(par)));
      }
      Reference call = RefFactory.call(map.get(func), acpar);
      cfunc.body.statements.add(new ExpressionReturn(new ReferenceExpression(call)));

      obj.item.add(cfunc); // TODO ok or copy?
    }

    return null;
  }

  static private void addQuery(AstList<Response> queries, Response query) {
    if (!queries.contains(query)) {
      queries.add(query);
    } else {
      // Fixme oops, what now? Just remove it?
      assert (false);
    }
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, QueryParam param) {
    Map<State, Integer> spos = new HashMap<State, Integer>();
    AstList<Response> queryList = new AstList<Response>();
    AstList<State> stateList = new AstList<State>();

    for (StateContent itr : obj.item) {
      if (itr instanceof Response) {
        queryList.add((Response) itr);
      } else if (itr instanceof State) {
        spos.put((State) itr, queryList.size());
        stateList.add((State) itr);
      }
    }

    obj.item.removeAll(queryList);

    // build parameter for every substate
    Map<State, QueryParam> spar = new HashMap<State, QueryParam>();
    for (State itr : stateList) {
      int idx = spos.get(itr);

      QueryParam cpar = new QueryParam(param);
      cpar.before.addAll(queryList.subList(0, idx));
      cpar.after.addAll(0, queryList.subList(idx, queryList.size()));

      spar.put(itr, cpar);
    }

    for (State itr : stateList) {
      visit(itr, spar.get(itr));
    }

    return null;
  }
}

class QueryParam {

  final public AstList<Response> before;
  final public AstList<Response> after;

  public QueryParam(AstList<Response> before, AstList<Response> after) {
    super();
    this.before = new AstList<Response>(before);
    this.after = new AstList<Response>(after);
  }

  public QueryParam() {
    super();
    this.before = new AstList<Response>();
    this.after = new AstList<Response>();
  }

  public QueryParam(QueryParam parent) {
    super();
    this.before = new AstList<Response>(parent.before);
    this.after = new AstList<Response>(parent.after);
  }
}

class QueryFuncMaker extends NullDispatcher<Void, Designator> {

  private final Map<Response, FuncFunction> qfunc;

  public QueryFuncMaker(Map<Response, FuncFunction> qfunc) {
    this.qfunc = qfunc;
  }

  @Override
  protected Void visitDefault(Ast obj, Designator param) {
    if (obj instanceof StateContent) {
      return null;
    } else {
      throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitFuncResponse(Response obj, Designator param) {
    param = new Designator(param, obj.getName());
    FuncFunction func = new FuncFunction(param.toString(), Copy.copy(obj.param), Copy.copy(obj.ret), obj.body);
    obj.body = new Block();

    FsmReduction.relinkActualParameterRef(obj.param, func.param, func.body);

    qfunc.put(obj, func);

    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    param = new Designator(param, obj.getName());
    visitList(obj.item, param);
    return null;
  }

}
