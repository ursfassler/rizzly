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

import ast.data.component.hfsm.State;
import ast.data.component.hfsm.Transition;
import ast.data.raw.RawHfsm;
import ast.repository.query.ChildByName;
import ast.repository.query.Single;
import ast.repository.query.TypeFilter;
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
  final private Single single = new Single(RError.instance());
  final private ChildByName childByName = new ChildByName(single);
  final private SubLinker linker = new SubLinker(childByName);

  public void process(RawHfsm obj) {
    visitState(obj.getTopstate());
  }

  private void visitState(State state) {
    for (Transition tr : TypeFilter.select(state.item, Transition.class)) {
      linker.link(tr.src.ref, state);
      linker.link(tr.dst.ref, state);
    }

    for (State sub : TypeFilter.select(state.item, State.class)) {
      visitState(sub);
    }
  }
}
