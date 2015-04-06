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

package evl.pass.hfsmreduction;

import java.util.ArrayList;
import java.util.List;

import pass.EvlPass;

import common.Designator;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.StateComposite;
import evl.data.component.hfsm.StateSimple;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;
import evl.traverser.other.ClassGetter;

/**
 * Moves all leaf-states up. In the end, the top state only has former leaf states a children.
 *
 * (The leaf states are the only states the state machine can be in.)
 *
 * @author urs
 *
 */

public class LeafStateUplifter extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    for (ImplHfsm hfsm : ClassGetter.getRecursive(ImplHfsm.class, evl)) {
      LeafStateUplifterWorker know = new LeafStateUplifterWorker(kb);
      know.traverse(hfsm, null);
    }
  }

}

class LeafStateUplifterWorker extends NullTraverser<Void, Designator> {
  final private List<StateSimple> states = new ArrayList<StateSimple>();

  public LeafStateUplifterWorker(KnowledgeBase kb) {
    super();
  }

  @Override
  protected Void visitDefault(Evl obj, Designator param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Designator param) {
    visit(obj.topstate, new Designator());
    obj.topstate.item.addAll(states);
    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, Designator param) {
    obj.name = param.toString(Designator.NAME_SEP);
    states.add(obj);
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, Designator param) {
    EvlList<State> children = obj.item.getItems(State.class);
    visitList(children, param);
    obj.item.removeAll(children);
    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    param = new Designator(param, obj.name);
    return super.visitState(obj, param);
  }

}
