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

import pass.FunPass;
import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.knowledge.KnowChild;
import fun.knowledge.KnowledgeBase;
import fun.other.Named;
import fun.other.Namespace;
import fun.other.RizzlyFile;

/**
 * Follows offset when link is to namespace until it finds a different object.
 *
 * Example: makes foo.bar() to bar()
 *
 * @author urs
 *
 */
public class NamespaceLinkReduction extends FunPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    NamespaceLinkReductionWorker reduction = new NamespaceLinkReductionWorker();
    reduction.traverse(root, null);
  }

}

class NamespaceLinkReductionWorker extends DefTraverser<Void, Void> {

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Fun item = obj.getLink();
    assert (!(item instanceof DummyLinkTarget));
    while (item instanceof Namespace) {
      RefItem next = obj.getOffset().get(0);
      obj.getOffset().remove(0);
      if (!(next instanceof RefName)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName());
      }
      RefName name = (RefName) next;
      ((Namespace) item).getChildren().find(name.getName());
      assert (item != null); // type checker should find it?
    }
    if (item instanceof RizzlyFile) {
      RefItem next = obj.getOffset().get(0);
      obj.getOffset().remove(0);
      if (!(next instanceof RefName)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName());
      }
      RefName name = (RefName) next;
      KnowChild kfc = new KnowChild();
      item = kfc.get(item, name.getName());
      assert (item != null); // type checker should find it?
    }
    obj.setLink((Named) item);
    return super.visitReference(obj, param);
  }

}
