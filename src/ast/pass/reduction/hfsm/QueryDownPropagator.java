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

import ast.Designator;
import ast.ElementInfo;
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
import ast.data.expression.TupleValue;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.Reference;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncResponse;
import ast.data.statement.Block;
import ast.data.statement.ReturnExpr;
import ast.data.variable.Variable;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.traverser.NullTraverser;
import ast.traverser.other.ClassGetter;

public class QueryDownPropagator extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    for (ImplHfsm hfsm : ClassGetter.getRecursive(ImplHfsm.class, ast)) {
      process(hfsm, kb);
    }
  }

  private static void process(ImplHfsm hfsm, KnowledgeBase kb) {
    Map<FuncResponse, FuncFunction> qfunc = new HashMap<FuncResponse, FuncFunction>();
    QueryFuncMaker qfmaker = new QueryFuncMaker(qfunc);
    qfmaker.traverse(hfsm.topstate, new Designator());

    for (FuncFunction func : qfunc.values()) {
      hfsm.topstate.item.add(func);
    }

    QueryDownPropagatorWorker redirecter = new QueryDownPropagatorWorker(qfunc);
    redirecter.traverse(hfsm.topstate, new QueryParam());
  }

}

class QueryDownPropagatorWorker extends NullTraverser<Void, QueryParam> {
  private static final ElementInfo info = ElementInfo.NO;
  private final Map<FuncResponse, FuncFunction> map; // TODO do we need
                                                     // that?

  public QueryDownPropagatorWorker(Map<FuncResponse, FuncFunction> map) {
    this.map = map;
  }

  @Override
  protected Void visitDefault(Ast obj, QueryParam param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, QueryParam param) {
    AstList<FuncResponse> queryList = ClassGetter.filter(FuncResponse.class, obj.item);
    obj.item.removeAll(queryList);

    AstList<FuncResponse> queries = new AstList<FuncResponse>();

    for (FuncResponse query : param.before) {
      addQuery(queries, query);
    }
    for (FuncResponse query : queryList) {
      addQuery(queries, query);
    }
    for (FuncResponse query : param.after) {
      addQuery(queries, query);
    }

    for (FuncResponse func : queries) {
      FuncResponse cfunc = new FuncResponse(info, func.name, Copy.copy(func.param), Copy.copy(func.ret), new Block(info));

      TupleValue acpar = new TupleValue(info, new AstList<Expression>());
      for (Variable par : cfunc.param) {
        acpar.value.add(new Reference(info, par));
      }
      Reference call = new Reference(info, map.get(func));
      call.offset.add(new RefCall(info, acpar));
      cfunc.body.statements.add(new ReturnExpr(info, call));

      obj.item.add(cfunc); // TODO ok or copy?
    }

    return null;
  }

  static private void addQuery(AstList<FuncResponse> queries, FuncResponse query) {
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
    AstList<FuncResponse> queryList = new AstList<FuncResponse>();
    AstList<State> stateList = new AstList<State>();

    for (StateContent itr : obj.item) {
      if (itr instanceof FuncResponse) {
        queryList.add((FuncResponse) itr);
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

  final public AstList<FuncResponse> before;
  final public AstList<FuncResponse> after;

  public QueryParam(AstList<FuncResponse> before, AstList<FuncResponse> after) {
    super();
    this.before = new AstList<FuncResponse>(before);
    this.after = new AstList<FuncResponse>(after);
  }

  public QueryParam() {
    super();
    this.before = new AstList<FuncResponse>();
    this.after = new AstList<FuncResponse>();
  }

  public QueryParam(QueryParam parent) {
    super();
    this.before = new AstList<FuncResponse>(parent.before);
    this.after = new AstList<FuncResponse>(parent.after);
  }
}

class QueryFuncMaker extends NullTraverser<Void, Designator> {

  private final Map<FuncResponse, FuncFunction> qfunc;

  public QueryFuncMaker(Map<FuncResponse, FuncFunction> qfunc) {
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
  protected Void visitFuncResponse(FuncResponse obj, Designator param) {
    param = new Designator(param, obj.name);
    FuncFunction func = new FuncFunction(ElementInfo.NO, param.toString(), Copy.copy(obj.param), Copy.copy(obj.ret), obj.body);
    obj.body = new Block(ElementInfo.NO);

    FsmReduction.relinkActualParameterRef(obj.param, func.param, func.body);

    qfunc.put(obj, func);

    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    param = new Designator(param, obj.name);
    visitList(obj.item, param);
    return null;
  }

}
