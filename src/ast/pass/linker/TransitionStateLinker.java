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

package ast.pass.linker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ast.data.component.hfsm.State;
import ast.data.component.hfsm.Transition;
import ast.data.expression.reference.DummyLinkTarget;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.StateRef;
import ast.data.raw.RawHfsm;
import ast.specification.TypeFilter;
import error.ErrorType;
import error.RError;

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

  public static void process(RawHfsm obj) {
    StateCollector isc = new StateCollector();
    isc.makeTable(obj);

    visitState(obj.getTopstate(), isc);
  }

  private static void visitState(ast.data.component.hfsm.State state, StateCollector isc) {
    Map<String, State> sym = isc.getSymbols(state);
    Set<String> amb = isc.getAmbigous(state);

    for (ast.data.component.hfsm.Transition tr : TypeFilter.select(state.item, Transition.class)) {
      link(tr.src, sym, amb);
      link(tr.dst, sym, amb);
    }

    for (ast.data.component.hfsm.State sub : TypeFilter.select(state.item, State.class)) {
      visitState(sub, isc);
    }
  }

  private static void link(StateRef sref, Map<String, State> sym, Set<String> amb) {
    Reference ref = (Reference) sref;

    if (ref.link instanceof DummyLinkTarget) {
      String target = ((DummyLinkTarget) ref.link).name;
      if (amb.contains(target)) {
        RError.err(ErrorType.Error, ref.getInfo(), "State name is ambigous: " + target);
      } else {
        State st = sym.get(target);
        if (st == null) {
          RError.err(ErrorType.Error, ref.getInfo(), "State not found: " + target);
        } else {
          ref.link = st;
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

  public Map<String, State> getSymbols(ast.data.component.hfsm.State state) {
    return symbols.get(state);
  }

  public Set<String> getAmbigous(ast.data.component.hfsm.State state) {
    return ambigous.get(state);
  }

  public void makeTable(RawHfsm hfsm) {
    gatherNames(hfsm.getTopstate());
    visitState(hfsm.getTopstate(), 0);
  }

  private void gatherNames(ast.data.component.hfsm.State state) {
    for (State sub : TypeFilter.select(state.item, State.class)) {
      getNames().put(sub, sub.name);
      gatherNames(sub);
    }
  }

  protected Set<State> visitState(State state, int param) {
    deepth.put(state, param);
    Set<State> all = new HashSet<State>();
    all.add(state);
    for (State sub : TypeFilter.select(state.item, State.class)) {
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
      ast.data.component.hfsm.State os = sym.get(getNames().get(st));
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
