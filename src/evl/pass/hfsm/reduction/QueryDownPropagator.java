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

package evl.pass.hfsm.reduction;

import java.util.HashMap;
import java.util.Map;

import pass.EvlPass;

import common.Designator;
import common.ElementInfo;

import evl.copy.Copy;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.StateComposite;
import evl.data.component.hfsm.StateItem;
import evl.data.component.hfsm.StateSimple;
import evl.data.expression.Expression;
import evl.data.expression.TupleValue;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.Reference;
import evl.data.function.header.FuncCtrlInDataOut;
import evl.data.function.header.FuncPrivateRet;
import evl.data.statement.Block;
import evl.data.statement.ReturnExpr;
import evl.data.variable.Variable;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;
import evl.traverser.other.ClassGetter;

public class QueryDownPropagator extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    for (ImplHfsm hfsm : ClassGetter.get(ImplHfsm.class, evl)) {
      process(hfsm, kb);
    }
  }

  private static void process(ImplHfsm hfsm, KnowledgeBase kb) {
    Map<FuncCtrlInDataOut, FuncPrivateRet> qfunc = new HashMap<FuncCtrlInDataOut, FuncPrivateRet>();
    QueryFuncMaker qfmaker = new QueryFuncMaker(qfunc);
    qfmaker.traverse(hfsm.topstate, new Designator());

    for (FuncPrivateRet func : qfunc.values()) {
      hfsm.topstate.item.add(func);
    }

    QueryDownPropagatorWorker redirecter = new QueryDownPropagatorWorker(qfunc);
    redirecter.traverse(hfsm.topstate, new QueryParam());
  }

}

class QueryDownPropagatorWorker extends NullTraverser<Void, QueryParam> {
  private static final ElementInfo info = ElementInfo.NO;
  private final Map<FuncCtrlInDataOut, FuncPrivateRet> map; // TODO do we need that?

  public QueryDownPropagatorWorker(Map<FuncCtrlInDataOut, FuncPrivateRet> map) {
    this.map = map;
  }

  @Override
  protected Void visitDefault(Evl obj, QueryParam param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, QueryParam param) {
    EvlList<FuncCtrlInDataOut> queryList = obj.item.getItems(FuncCtrlInDataOut.class);
    obj.item.removeAll(queryList);

    EvlList<FuncCtrlInDataOut> queries = new EvlList<FuncCtrlInDataOut>();

    for (FuncCtrlInDataOut query : param.before) {
      addQuery(queries, query);
    }
    for (FuncCtrlInDataOut query : queryList) {
      addQuery(queries, query);
    }
    for (FuncCtrlInDataOut query : param.after) {
      addQuery(queries, query);
    }

    for (FuncCtrlInDataOut func : queries) {
      FuncCtrlInDataOut cfunc = new FuncCtrlInDataOut(info, func.name, Copy.copy(func.param), Copy.copy(func.ret), new Block(info));

      TupleValue acpar = new TupleValue(info, new EvlList<Expression>());
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

  static private void addQuery(EvlList<FuncCtrlInDataOut> queries, FuncCtrlInDataOut query) {
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
    EvlList<FuncCtrlInDataOut> queryList = new EvlList<FuncCtrlInDataOut>();
    EvlList<State> stateList = new EvlList<State>();

    for (StateItem itr : obj.item) {
      if (itr instanceof FuncCtrlInDataOut) {
        queryList.add((FuncCtrlInDataOut) itr);
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

  final public EvlList<FuncCtrlInDataOut> before;
  final public EvlList<FuncCtrlInDataOut> after;

  public QueryParam(EvlList<FuncCtrlInDataOut> before, EvlList<FuncCtrlInDataOut> after) {
    super();
    this.before = new EvlList<FuncCtrlInDataOut>(before);
    this.after = new EvlList<FuncCtrlInDataOut>(after);
  }

  public QueryParam() {
    super();
    this.before = new EvlList<FuncCtrlInDataOut>();
    this.after = new EvlList<FuncCtrlInDataOut>();
  }

  public QueryParam(QueryParam parent) {
    super();
    this.before = new EvlList<FuncCtrlInDataOut>(parent.before);
    this.after = new EvlList<FuncCtrlInDataOut>(parent.after);
  }
}

class QueryFuncMaker extends NullTraverser<Void, Designator> {

  private final Map<FuncCtrlInDataOut, FuncPrivateRet> qfunc;

  public QueryFuncMaker(Map<FuncCtrlInDataOut, FuncPrivateRet> qfunc) {
    this.qfunc = qfunc;
  }

  @Override
  protected Void visitDefault(Evl obj, Designator param) {
    if (obj instanceof StateItem) {
      return null;
    } else {
      throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitFuncIfaceInRet(FuncCtrlInDataOut obj, Designator param) {
    param = new Designator(param, obj.name);
    FuncPrivateRet func = new FuncPrivateRet(ElementInfo.NO, param.toString(), Copy.copy(obj.param), Copy.copy(obj.ret), obj.body);
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
