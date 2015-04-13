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
import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.type.base.EnumElement;
import evl.data.type.base.EnumType;
import evl.knowledge.KnowChild;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;

/**
 * Changes references to enums, e.g. Weekday.Tuesday -> Tuesday
 *
 * @author urs
 *
 */
public class EnumLinkReduction extends EvlPass {

  @Override
  public void process(evl.data.Namespace root, KnowledgeBase kb) {
    EnumLinkReductionWorker reduction = new EnumLinkReductionWorker(kb);
    reduction.traverse(root, null);
  }

}

class EnumLinkReductionWorker extends DefTraverser<Void, Void> {
  private final KnowChild kc;

  public EnumLinkReductionWorker(KnowledgeBase kb) {
    kc = kb.getEntry(KnowChild.class);
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Evl item = obj.link;
    if (item instanceof EnumType) {
      if (!obj.offset.isEmpty()) {
        evl.data.expression.reference.RefItem next = obj.offset.get(0);
        obj.offset.remove(0);
        if (!(next instanceof RefName)) {
          RError.err(ErrorType.Error, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName());
        }
        Evl elem = kc.find(item, ((evl.data.expression.reference.RefName) next).name);
        if (elem == null) {
          RError.err(ErrorType.Error, obj.getInfo(), "Element not found: " + ((evl.data.expression.reference.RefName) next).name);
        }
        if (elem instanceof EnumElement) {
          obj.link = (EnumElement) elem;
        } else {
          RError.err(ErrorType.Error, obj.getInfo(), "Expected enumerator element, got: " + elem.getClass().getCanonicalName());
        }
      }
    }
    return super.visitReference(obj, param);
  }
}
