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

package evl.pass.hfsm.reduction;

import pass.EvlPass;
import evl.data.Evl;
import evl.data.Namespace;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.StateItem;
import evl.data.component.hfsm.Transition;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;
import evl.traverser.other.ClassGetter;

/**
 * Sets the destination of a transition to the initial substate if the destination is a composite state
 *
 * @author urs
 *
 */
public class TransitionRedirecter extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    for (ImplHfsm hfsm : ClassGetter.getRecursive(ImplHfsm.class, evl)) {
      TransitionRedirecterWorker redirecter = new TransitionRedirecterWorker();
      redirecter.traverse(hfsm.topstate, null);
    }
  }
}

class TransitionRedirecterWorker extends NullTraverser<Void, Void> {
  final private InitStateGetter initStateGetter = new InitStateGetter();

  public static void process(State top) {
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    if (obj instanceof StateItem) {
      return null;
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitList(obj.item, null);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    State dst = obj.dst.link;
    dst = initStateGetter.traverse(dst, null);
    obj.dst.link = dst;
    return null;
  }

}
