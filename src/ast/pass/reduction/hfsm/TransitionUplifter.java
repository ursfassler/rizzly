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

package ast.pass.reduction.hfsm;

import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.Transition;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.Collector;
import ast.repository.Manipulate;
import ast.specification.IsClass;

/**
 * Moves all transitions of all states to the top-state.
 *
 * @author urs
 *
 */

public class TransitionUplifter extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    AstList<ImplHfsm> hfsmList = Collector.select(ast, new IsClass(ImplHfsm.class)).castTo(ImplHfsm.class);
    for (ImplHfsm hfsm : hfsmList) {
      StateComposite topstate = hfsm.topstate;
      moveAllTransitionToTop(topstate);
    }
  }

  private void moveAllTransitionToTop(StateComposite topstate) {
    final IsClass isTransition = new IsClass(Transition.class);
    AstList<Transition> transitions = Collector.select(topstate, isTransition).castTo(Transition.class);
    Manipulate.remove(topstate, isTransition);
    topstate.item.addAll(transitions);
  }

}
