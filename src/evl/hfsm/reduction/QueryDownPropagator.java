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

package evl.hfsm.reduction;

import java.util.HashMap;
import java.util.Map;

import common.Designator;
import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.copy.Copy;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.Reference;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncPrivateRet;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateItem;
import evl.hfsm.StateSimple;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.statement.Block;
import evl.statement.ReturnExpr;
import evl.variable.Variable;

public class QueryDownPropagator extends NullTraverser<Void, QueryParam> {
  private static final ElementInfo info = ElementInfo.NO;
  private final Map<FuncCtrlInDataOut, FuncPrivateRet> map; // TODO do we need that?

  public QueryDownPropagator(Map<FuncCtrlInDataOut, FuncPrivateRet> map) {
    this.map = map;
  }

  public static void process(ImplHfsm hfsm, KnowledgeBase kb) {
    Map<FuncCtrlInDataOut, FuncPrivateRet> qfunc = new HashMap<FuncCtrlInDataOut, FuncPrivateRet>();
    QueryFuncMaker qfmaker = new QueryFuncMaker(qfunc);
    qfmaker.traverse(hfsm.getTopstate(), new Designator());

    for (FuncPrivateRet func : qfunc.values()) {
      hfsm.getTopstate().getItem().add(func);
    }

    QueryDownPropagator redirecter = new QueryDownPropagator(qfunc);
    redirecter.traverse(hfsm.getTopstate(), new QueryParam());
  }

  @Override
  protected Void visitDefault(Evl obj, QueryParam param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, QueryParam param) {
    EvlList<FuncCtrlInDataOut> queryList = obj.getItem().getItems(FuncCtrlInDataOut.class);
    obj.getItem().removeAll(queryList);

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
      FuncCtrlInDataOut cfunc = new FuncCtrlInDataOut(info, func.getName(), Copy.copy(func.getParam()), Copy.copy(func.getRet()), new Block(info));

      EvlList<Expression> acpar = new EvlList<Expression>();
      for (Variable par : cfunc.getParam()) {
        acpar.add(new Reference(info, par));
      }
      Reference call = new Reference(info, map.get(func));
      call.getOffset().add(new RefCall(info, acpar));
      cfunc.getBody().getStatements().add(new ReturnExpr(info, call));

      obj.getItem().add(cfunc); // TODO ok or copy?
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

    for (StateItem itr : obj.getItem()) {
      if (itr instanceof FuncCtrlInDataOut) {
        queryList.add((FuncCtrlInDataOut) itr);
      } else if (itr instanceof State) {
        spos.put((State) itr, queryList.size());
        stateList.add((State) itr);
      }
    }

    obj.getItem().removeAll(queryList);

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
    param = new Designator(param, obj.getName());
    FuncPrivateRet func = new FuncPrivateRet(ElementInfo.NO, param.toString(), Copy.copy(obj.getParam()), Copy.copy(obj.getRet()), obj.getBody());
    obj.setBody(new Block(ElementInfo.NO));

    FsmReduction.relinkActualParameterRef(obj.getParam(), func.getParam(), func.getBody());

    qfunc.put(obj, func);

    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    param = new Designator(param, obj.getName());
    visitList(obj.getItem(), param);
    return null;
  }

}
