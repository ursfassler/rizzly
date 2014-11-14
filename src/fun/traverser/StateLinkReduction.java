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

package fun.traverser;

import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.hfsm.State;
import fun.knowledge.KnowChild;
import fun.knowledge.KnowledgeBase;
import fun.other.Named;

/**
 * Changes references to deepest state, e.g. _top.A.B -> B
 *
 * @author urs
 *
 */
public class StateLinkReduction extends DefTraverser<Void, Void> {
  private final KnowChild kc;

  public StateLinkReduction(KnowledgeBase kb) {
    kc = kb.getEntry(KnowChild.class);
  }

  public static void process(Fun inst, KnowledgeBase kb) {
    StateLinkReduction reduction = new StateLinkReduction(kb);
    reduction.traverse(inst, null);
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Fun item = obj.getLink();
    if (item instanceof State) {
      while (!obj.getOffset().isEmpty()) {
        RefItem next = obj.getOffset().get(0);
        obj.getOffset().remove(0);
        RefName name = (RefName) next;

        item = kc.get(item, name.getName());
        assert (item != null);
        if (!(item instanceof State)) {
          break;
        }
      }
      obj.setLink((Named) item);
      obj.getOffset().clear();
    }
    return super.visitReference(obj, param);
  }
}
