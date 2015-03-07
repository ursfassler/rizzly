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

package evl.pass.check;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pass.EvlPass;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.Transition;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.traverser.ClassGetter;

/**
 * Checks if the states (src,dst) of a transition are reachable from the transition (inner scope)
 *
 * @author urs
 */
public class HfsmTransScopeCheck extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    HfsmTransScopeCheckWorker check = new HfsmTransScopeCheckWorker();
    List<ImplHfsm> hfsms = ClassGetter.get(ImplHfsm.class, evl);
    for (ImplHfsm hfsm : hfsms) {
      check.traverse(hfsm, null);
    }
  }

}

class HfsmTransScopeCheckWorker extends NullTraverser<Set<State>, Void> {

  @Override
  protected Set<State> visitDefault(Evl obj, Void param) {
    throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Set<State> visitImplHfsm(ImplHfsm obj, Void param) {
    List<State> states = obj.getTopstate().getItem().getItems(State.class);
    for (State subState : states) {
      visit(subState, null);
    }
    return null;
  }

  @Override
  protected Set<State> visitState(State obj, Void param) {
    Set<State> ret = new HashSet<State>();
    ret.add(obj);

    for (State subState : obj.getItem().getItems(State.class)) {
      ret.addAll(visit(subState, null));
    }

    for (Transition trans : obj.getItem().getItems(Transition.class)) {
      checkTransition(trans, ret);
    }

    return ret;
  }

  private void checkTransition(Transition trans, Set<State> allowed) {
    check(trans.getSrc().getLink(), allowed, trans.getInfo(), "source");
    check(trans.getDst().getLink(), allowed, trans.getInfo(), "destination");
  }

  private void check(State state, Set<State> allowed, ElementInfo info, String end) {
    if (!allowed.contains(state)) {
      RError.err(ErrorType.Error, info, "Connection to state which is in outer scope for " + end + " (" + state.getName() + ")");
    }
  }
}