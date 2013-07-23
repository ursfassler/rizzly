package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Pair;
import evl.Evl;
import evl.NullTraverser;
import evl.copy.Copy;
import evl.hfsm.HfsmQueryFunction;
import evl.hfsm.ImplHfsm;
import evl.hfsm.QueryItem;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateItem;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.knowledge.KnowledgeBase;

//TODO do not duplicate query in simple state when query is defined in parent state (call query of parent state)
public class QueryDownPropagator extends NullTraverser<Void, QueryParam> {

  public static void process(ImplHfsm hfsm, KnowledgeBase kb) {
    QueryDownPropagator redirecter = new QueryDownPropagator();
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
      HfsmQueryFunction cfunc = Copy.copy(func);
      obj.getItem().add(new QueryItem(key.first, cfunc));
    }

    return null;
  }

  private void addQuery(Map<Pair<String, String>, HfsmQueryFunction> set, QueryItem query) {
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
