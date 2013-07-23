package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import evl.Evl;
import evl.NullTraverser;
import evl.hfsm.QueryItem;
import evl.hfsm.State;
import evl.hfsm.Transition;

public class TransitionDict extends NullTraverser<Void, Void> {
  private Map<State, Map<String, Map<String, List<Transition>>>> transition = new HashMap<State, Map<String, Map<String, List<Transition>>>>();

  public List<Transition> get(State src, String iface, String func) {
    Map<String, Map<String, List<Transition>>> smap = transition.get(src);
    if (smap == null) {
      smap = new HashMap<String, Map<String, List<Transition>>>();
      transition.put(src, smap);
    }
    Map<String, List<Transition>> imap = smap.get(iface);
    if (imap == null) {
      imap = new HashMap<String, List<Transition>>();
      smap.put(iface, imap);
    }
    List<Transition> fmap = imap.get(func);
    if (fmap == null) {
      fmap = new ArrayList<Transition>();
      imap.put(func, fmap);
    }
    return fmap;
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitQueryItem(QueryItem obj, Void param) {
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
    String iface = obj.getEventIface().getName();
    String func = obj.getEventFunc();
    List<Transition> list = get(src, iface, func);
    list.add(obj);

    return null;
  }

}
