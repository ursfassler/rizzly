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

package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.List;

import pass.EvlPass;
import evl.Evl;
import evl.NullTraverser;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateItem;
import evl.hfsm.Transition;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;

/**
 * Moves all transitions of all states to the top-state.
 *
 * @author urs
 *
 */

public class TransitionUplifter extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    for (ImplHfsm hfsm : evl.getItems(ImplHfsm.class, true)) {
      TransitionUplifterWorker know = new TransitionUplifterWorker();
      know.traverse(hfsm, null);
    }
  }

}

class TransitionUplifterWorker extends NullTraverser<Void, List<Transition>> {

  @Override
  protected Void visitDefault(Evl obj, List<Transition> param) {
    if (obj instanceof StateItem) {
      return null;
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, List<Transition> param) {
    List<Transition> list = new ArrayList<Transition>();
    visit(obj.getTopstate(), list);
    obj.getTopstate().getItem().addAll(list);
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, List<Transition> param) {
    visitList(obj.getItem(), param);
    return null;
  }

  @Override
  protected Void visitState(State obj, List<Transition> param) {
    List<Transition> transList = obj.getItem().getItems(Transition.class);
    param.addAll(transList);
    obj.getItem().removeAll(transList);
    return super.visitState(obj, param);
  }

}
