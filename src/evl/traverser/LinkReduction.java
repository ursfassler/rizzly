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

package evl.traverser;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.other.Named;
import evl.other.Namespace;

/**
 * Follows offset when link is to namespace until it finds a different object.
 *
 * Example: makes foo.bar() to bar()
 *
 * @author urs
 *
 */
public class LinkReduction extends DefTraverser<Void, Void> {

  public static void process(Evl inst) {
    LinkReduction reduction = new LinkReduction();
    reduction.traverse(inst, null);
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Evl item = obj.getLink();
    while (item instanceof Namespace) {
      RefItem next = obj.getOffset().get(0);
      obj.getOffset().remove(0);
      if (!(next instanceof RefName)) {
        // TODO check it with typechecker
        RError.err(ErrorType.Fatal, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName() + " (and why did the typechecker not find it?)");
      }
      RefName name = (RefName) next;
      item = ((Namespace) item).getChildren().find(name.getName());
      assert (item != null); // type checker should find it?
    }
    obj.setLink((Named) item);
    return super.visitReference(obj, param);
  }

}
