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

package fun.pass;

import pass.EvlPass;
import evl.data.Evl;
import evl.data.Named;
import evl.data.component.hfsm.State;
import evl.data.expression.reference.Reference;
import evl.knowledge.KnowChild;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;

/**
 * Changes references to deepest state, e.g. _top.A.B -> B
 *
 * @author urs
 *
 */
public class StateLinkReduction extends EvlPass {

  @Override
  public void process(evl.data.Namespace root, KnowledgeBase kb) {
    StateLinkReductionWorker reduction = new StateLinkReductionWorker(kb);
    reduction.traverse(root, null);
  }
}

class StateLinkReductionWorker extends DefTraverser<Void, Void> {
  private final KnowChild kc;

  public StateLinkReductionWorker(KnowledgeBase kb) {
    kc = kb.getEntry(KnowChild.class);
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Evl item = obj.link;
    if (item instanceof State) {
      while (!obj.offset.isEmpty()) {
        evl.data.expression.reference.RefItem next = obj.offset.get(0);
        obj.offset.remove(0);
        evl.data.expression.reference.RefName name = (evl.data.expression.reference.RefName) next;

        item = kc.get(item, name.name, item.getInfo());
        assert (item != null);
        if (!(item instanceof State)) {
          break;
        }
      }
      obj.link = (Named) item;
      obj.offset.clear();
    }
    return super.visitReference(obj, param);
  }
}
