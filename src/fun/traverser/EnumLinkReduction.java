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

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.knowledge.KnowChild;
import fun.knowledge.KnowledgeBase;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;

/**
 * Changes references to enums, e.g. Weekday.Tuesday -> Tuesday
 *
 * @author urs
 *
 */
public class EnumLinkReduction extends DefTraverser<Void, Void> {
  private final KnowChild kc;

  public EnumLinkReduction(KnowledgeBase kb) {
    kc = kb.getEntry(KnowChild.class);
  }

  public static void process(Fun inst, KnowledgeBase kb) {
    EnumLinkReduction reduction = new EnumLinkReduction(kb);
    reduction.traverse(inst, null);
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Fun item = obj.getLink();
    if (item instanceof EnumType) {
      if (!obj.getOffset().isEmpty()) {
        RefItem next = obj.getOffset().get(0);
        obj.getOffset().remove(0);
        if (!(next instanceof RefName)) {
          RError.err(ErrorType.Error, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName());
        }
        Fun elem = kc.find(item, ((RefName) next).getName());
        if (elem == null) {
          RError.err(ErrorType.Error, obj.getInfo(), "Element not found: " + ((RefName) next).getName());
        }
        if (elem instanceof EnumElement) {
          obj.setLink((EnumElement) elem);
        } else {
          RError.err(ErrorType.Error, obj.getInfo(), "Expected enumerator element, got: " + elem.getClass().getCanonicalName());
        }
      }
    }
    return super.visitReference(obj, param);
  }
}
