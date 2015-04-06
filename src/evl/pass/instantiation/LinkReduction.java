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

package evl.pass.instantiation;

import pass.EvlPass;
import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.Named;
import evl.data.Namespace;
import evl.data.expression.reference.RefItem;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.knowledge.KnowChild;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;

/**
 * Follows offset when link is to namespace until it finds a different object.
 *
 * Example: makes foo.bar() to bar()
 *
 * @author urs
 *
 */
public class LinkReduction extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    // FIXME hacky
    KnowChild kc = kb.getEntry(KnowChild.class);
    Namespace inst = (Namespace) kc.find(evl, "!inst");

    LinkReductionWorker reduction = new LinkReductionWorker();
    reduction.traverse(inst, null);
  }
}

class LinkReductionWorker extends DefTraverser<Void, Void> {

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Evl item = obj.link;
    while (item instanceof Namespace) {
      RefItem next = obj.offset.get(0);
      obj.offset.remove(0);
      if (!(next instanceof RefName)) {
        // TODO check it with typechecker
        RError.err(ErrorType.Fatal, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName() + " (and why did the typechecker not find it?)");
      }
      RefName name = (RefName) next;
      item = ((Namespace) item).children.find(name.name);
      assert (item != null); // type checker should find it?
    }
    obj.link = (Named) item;
    return super.visitReference(obj, param);
  }

}
