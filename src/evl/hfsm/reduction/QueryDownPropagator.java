package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Pair;

import common.Designator;
import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.copy.Copy;
import evl.expression.Expression;
import evl.expression.reference.RefCall;
import evl.expression.reference.Reference;
import evl.function.impl.FuncPrivateRet;
import evl.hfsm.HfsmQueryFunction;
import evl.hfsm.ImplHfsm;
import evl.hfsm.QueryItem;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateItem;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.knowledge.KnowledgeBase;
import evl.other.ListOfNamed;
import evl.statement.Block;
import evl.statement.ReturnExpr;
import evl.variable.FuncVariable;
import evl.variable.Variable;

public class QueryDownPropagator extends NullTraverser<Void, QueryParam> {

  private static final ElementInfo info = new ElementInfo();
  private final Map<HfsmQueryFunction, FuncPrivateRet> map;

  public QueryDownPropagator(Map<HfsmQueryFunction, FuncPrivateRet> map) {
    this.map = map;
  }

  public static void process(ImplHfsm hfsm, KnowledgeBase kb) {
    Map<HfsmQueryFunction, FuncPrivateRet> qfunc = new HashMap<HfsmQueryFunction, FuncPrivateRet>();
    QueryFuncMaker qfmaker = new QueryFuncMaker(qfunc);
    qfmaker.traverse(hfsm.getTopstate(), new Designator());

    for (FuncPrivateRet func : qfunc.values()) {
      hfsm.getTopstate().getFunction().add(func);
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
    List<QueryItem> queryList = obj.getItemList(QueryItem.class);
    obj.getItem().removeAll(queryList);

    Map<Pair<String, String>, HfsmQueryFunction> queries = new HashMap<Pair<String, String>, HfsmQueryFunction>();

    for (QueryItem query : param.before) {
      addQuery(queries, query);
    }
    for (QueryItem query : queryList) {
      addQuery(queries, query);
    }
    for (QueryItem query : param.after) {
      addQuery(queries, query);
    }

    for (Pair<String, String> key : queries.keySet()) {
      HfsmQueryFunction func = queries.get(key);

      HfsmQueryFunction cfunc = new HfsmQueryFunction(info, func.getName(), new ListOfNamed<FuncVariable>(Copy.copy(func.getParam().getList())));
      cfunc.setRet(func.getRet().copy());

      cfunc.setBody(new Block(info));

      ArrayList<Expression> acpar = new ArrayList<Expression>();
      for (Variable par : cfunc.getParam()) {
        acpar.add(new Reference(info, par));
      }
      Reference call = new Reference(info, map.get(func));
      call.getOffset().add(new RefCall(info, acpar));
      cfunc.getBody().getStatements().add(new ReturnExpr(info, call));

      obj.getItem().add(new QueryItem(key.first, cfunc));
    }

    return null;
  }

  static private void addQuery(Map<Pair<String, String>, HfsmQueryFunction> set, QueryItem query) {
    Pair<String, String> key = new Pair<String, String>(query.getNamespace(), query.getFunc().getName());

    if (!set.containsKey(key)) {
      set.put(key, query.getFunc());
    }
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, QueryParam param) {
    Map<State, Integer> spos = new HashMap<State, Integer>();
    ArrayList<QueryItem> queryList = new ArrayList<QueryItem>();
    ArrayList<State> stateList = new ArrayList<State>();

    for (StateItem itr : obj.getItem()) {
      if (itr instanceof QueryItem) {
        queryList.add((QueryItem) itr);
      } else if (itr instanceof State) {
        spos.put((State) itr, queryList.size());
        stateList.add((State) itr);
      } else {
        assert (itr instanceof Transition);
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

  final public ArrayList<QueryItem> before;
  final public ArrayList<QueryItem> after;

  public QueryParam(List<QueryItem> before, List<QueryItem> after) {
    super();
    this.before = new ArrayList<QueryItem>(before);
    this.after = new ArrayList<QueryItem>(after);
  }

  public QueryParam() {
    super();
    this.before = new ArrayList<QueryItem>();
    this.after = new ArrayList<QueryItem>();
  }

  public QueryParam(QueryParam parent) {
    super();
    this.before = new ArrayList<QueryItem>(parent.before);
    this.after = new ArrayList<QueryItem>(parent.after);
  }
}

class QueryFuncMaker extends NullTraverser<Void, Designator> {

  private final Map<HfsmQueryFunction, FuncPrivateRet> qfunc;

  public QueryFuncMaker(Map<HfsmQueryFunction, FuncPrivateRet> qfunc) {
    this.qfunc = qfunc;
  }

  @Override
  protected Void visitDefault(Evl obj, Designator param) {
    throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitHfsmQueryFunction(HfsmQueryFunction obj, Designator param) {
    param = new Designator(param, obj.getName());

    Collection<FuncVariable> params = Copy.copy(obj.getParam().getList());
    ElementInfo info = new ElementInfo();
    FuncPrivateRet func = new FuncPrivateRet(info, param.toString(Designator.NAME_SEP), new ListOfNamed<FuncVariable>(params));
    func.setRet(obj.getRet().copy());
    func.setBody(obj.getBody());
    obj.setBody(new Block(info));

    HfsmReduction.relinkActualParameterRef(obj.getParam(), func.getParam().getList(), func.getBody());

    qfunc.put(obj, func);

    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    param = new Designator(param, obj.getName());
    visitItr(obj.getItem(), param);
    return null;
  }

  @Override
  protected Void visitQueryItem(QueryItem obj, Designator param) {
    param = new Designator(param, obj.getNamespace());
    visit(obj.getFunc(), param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Designator param) {
    return null;
  }
}
