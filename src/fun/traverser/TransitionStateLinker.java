package fun.traverser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import error.ErrorType;
import error.RError;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.Reference;
import fun.hfsm.ImplHfsm;
import fun.hfsm.State;
import fun.hfsm.Transition;

/**
 * Links all src/dst states of all transitions in a hfsm implementation.
 * 
 * This is needed since the visibility of states is different than other rules. A transition can only come from / go to
 * a state that is on the same or a deeper level than the transition is defined.
 * 
 * @author urs
 * 
 */
public class TransitionStateLinker {

  public static void process(ImplHfsm obj) {
    StateCollector isc = new StateCollector();
    isc.makeTable(obj);

    visitState(obj.getTopstate(), isc);
  }

  private static void visitState(State state, StateCollector isc) {
    Map<String, State> sym = isc.getSymbols(state);
    Set<String> amb = isc.getAmbigous(state);

    for (Transition tr : state.getItemList().getItems(Transition.class)) {
      link(tr.getSrc(), sym, amb);
      link(tr.getDst(), sym, amb);
    }

    for (State sub : state.getItemList().getItems(State.class)) {
      visitState(sub, isc);
    }
  }

  private static void link(Reference src, Map<String, State> sym, Set<String> amb) {
    if (src.getLink() instanceof DummyLinkTarget) {
      String target = ((DummyLinkTarget) src.getLink()).getName();
      if (amb.contains(target)) {
        RError.err(ErrorType.Error, src.getInfo(), "State name is ambigous: " + target);
      } else {
        State st = sym.get(target);
        if (st == null) {
          RError.err(ErrorType.Error, src.getInfo(), "State not found: " + target);
        } else {
          src.setLink(st);
        }
      }
    }
  }

}

class StateCollector {
  final private Map<State, Integer> deepth = new HashMap<State, Integer>();
  final private Map<State, Map<String, State>> symbols = new HashMap<State, Map<String, State>>();
  final private Map<State, Set<String>> ambigous = new HashMap<State, Set<String>>();
  final private Map<State, String> names = new HashMap<State, String>();

  public Map<String, State> getSymbols(State state) {
    return symbols.get(state);
  }

  public Set<String> getAmbigous(State state) {
    return ambigous.get(state);
  }

  public void makeTable(ImplHfsm hfsm) {
    gatherNames(hfsm.getTopstate());
    visitState(hfsm.getTopstate(), 0);
  }

  private void gatherNames(State state) {
    for (State sub : state.getItemList().getItems(State.class)) {
      getNames().put(sub, sub.getName());
      gatherNames(sub);
    }
  }

  protected Set<State> visitState(State state, int param) {
    deepth.put(state, param);
    Set<State> all = new HashSet<State>();
    all.add(state);
    for (State sub : state.getItemList().getItems(State.class)) {
      all.addAll(visitState(sub, param + 1));
    }

    Map<String, Integer> amb = new HashMap<String, Integer>();
    Map<String, State> sym = new HashMap<String, State>();

    for (State st : all) {
      add(st, sym, amb);
    }

    symbols.put(state, sym);
    ambigous.put(state, amb.keySet());

    return all;
  }

  private void add(State st, Map<String, State> sym, Map<String, Integer> amb) {
    Integer ad = amb.get(getNames().get(st));
    if (ad != null) {
      assert (!sym.containsKey(getNames().get(st)));
      int nd = deepth.get(st);
      if (nd < ad) {
        amb.remove(getNames().get(st));
        sym.put(getNames().get(st), st);
      }
    } else {
      State os = sym.get(getNames().get(st));
      if (os == null) {
        sym.put(getNames().get(st), st);
      } else {
        int nd = deepth.get(st);
        int od = deepth.get(os);
        if (nd > od) {
          sym.put(getNames().get(st), st);
        } else if (nd == od) {
          sym.remove(getNames().get(os));
          amb.put(getNames().get(os), od);
        } else {
          assert (nd < od);
        }
      }
    }
  }

  public Map<State, String> getNames() {
    return names;
  }

}
