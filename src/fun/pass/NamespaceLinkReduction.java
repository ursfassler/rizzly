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
import evl.data.Named;
import evl.data.Namespace;
import evl.data.expression.reference.DummyLinkTarget;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.file.RizzlyFile;
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
public class NamespaceLinkReduction extends EvlPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    NamespaceLinkReductionWorker reduction = new NamespaceLinkReductionWorker();
    reduction.traverse(root, null);
  }

}

class NamespaceLinkReductionWorker extends DefTraverser<Void, Void> {

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Evl item = obj.link;
    assert (!(item instanceof DummyLinkTarget));
    while (item instanceof Namespace) {
      evl.data.expression.reference.RefItem next = obj.offset.get(0);
      obj.offset.remove(0);
      if (!(next instanceof RefName)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName());
      }
      evl.data.expression.reference.RefName name = (evl.data.expression.reference.RefName) next;
      ((evl.data.Namespace) item).children.find(name.name);
      assert (item != null); // type checker should find it?
    }
    if (item instanceof RizzlyFile) {
      evl.data.expression.reference.RefItem next = obj.offset.get(0);
      obj.offset.remove(0);
      if (!(next instanceof RefName)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName());
      }
      evl.data.expression.reference.RefName name = (evl.data.expression.reference.RefName) next;
      KnowChild kfc = new KnowChild();
      item = kfc.get(item, name.name, item.getInfo());
      assert (item != null); // type checker should find it?
    }
    obj.link = (Named) item;
    return super.visitReference(obj, param);
  }

}
