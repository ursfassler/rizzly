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

package ast.pass.modelcheck;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pass.AstPass;
import ast.data.Ast;
import ast.data.Namespace;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.Transition;
import ast.knowledge.KnowledgeBase;
import ast.traverser.NullTraverser;
import ast.traverser.other.ClassGetter;

import common.ElementInfo;

import error.ErrorType;
import error.RError;

/**
 * Checks if the states (src,dst) of a transition are reachable from the transition (inner scope)
 *
 * @author urs
 */
public class HfsmTransScopeCheck extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    HfsmTransScopeCheckWorker check = new HfsmTransScopeCheckWorker();
    List<ImplHfsm> hfsms = ClassGetter.getRecursive(ImplHfsm.class, ast);
    for (ImplHfsm hfsm : hfsms) {
      check.traverse(hfsm, null);
    }
  }

}

class HfsmTransScopeCheckWorker extends NullTraverser<Set<State>, Void> {

  @Override
  protected Set<State> visitDefault(Ast obj, Void param) {
    throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Set<State> visitImplHfsm(ImplHfsm obj, Void param) {
    List<State> states = ClassGetter.filter(State.class, obj.topstate.item);
    for (State subState : states) {
      visit(subState, null);
    }
    return null;
  }

  @Override
  protected Set<State> visitState(State obj, Void param) {
    Set<State> ret = new HashSet<State>();
    ret.add(obj);

    for (State subState : ClassGetter.filter(State.class, obj.item)) {
      ret.addAll(visit(subState, null));
    }

    for (Transition trans : ClassGetter.filter(Transition.class, obj.item)) {
      checkTransition(trans, ret);
    }

    return ret;
  }

  private void checkTransition(Transition trans, Set<State> allowed) {
    check((State) trans.src.getTarget(), allowed, trans.getInfo(), "source");
    check((State) trans.dst.getTarget(), allowed, trans.getInfo(), "destination");
  }

  private void check(State state, Set<State> allowed, ElementInfo info, String end) {
    if (!allowed.contains(state)) {
      RError.err(ErrorType.Error, info, "Connection to state which is in outer scope for " + end + " (" + state.name + ")");
    }
  }
}
