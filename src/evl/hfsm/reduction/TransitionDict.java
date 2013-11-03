package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import evl.Evl;
import evl.NullTraverser;
import evl.hfsm.HfsmQueryFunction;
import evl.hfsm.State;
import evl.hfsm.Transition;

public class TransitionDict extends NullTraverser<Void, Void> {
  private Map<State, Map<String, List<Transition>>> transition = new HashMap<State, Map<String, List<Transition>>>();

  public List<Transition> get(State src, String func) {
    Map<String, List<Transition>> smap = transition.get(src);
    if (smap == null) {
      smap = new HashMap<String, List<Transition>>();
      transition.put(src, smap);
    }
    List<Transition> fmap = smap.get(func);
    if (fmap == null) {
      fmap = new ArrayList<Transition>();
      smap.put(func, fmap);
    }
    return fmap;
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitHfsmQueryFunction(HfsmQueryFunction obj, Void param) {
    return null;
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitItr(obj.getItem(), param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    State src = obj.getSrc();
    String func = obj.getEventFunc().getLink().getName();
    List<Transition> list = get(src, func);
    list.add(obj);

    return null;
  }

}
