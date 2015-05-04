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

package ast.pass.reduction;

import ast.data.Ast;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.expression.reference.DummyLinkTarget;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.Reference;
import ast.data.file.RizzlyFile;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.ChildCollector;
import ast.repository.NameFilter;
import ast.repository.Single;
import ast.specification.HasName;
import ast.traverser.DefTraverser;
import error.ErrorType;
import error.RError;

/**
 * Follows offset when link is to namespace until it finds a different object.
 *
 * Example: makes foo.bar() to bar()
 *
 * @author urs
 *
 */
public class NamespaceLinkReduction extends AstPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    NamespaceLinkReductionWorker reduction = new NamespaceLinkReductionWorker();
    reduction.traverse(root, null);
  }

}

class NamespaceLinkReductionWorker extends DefTraverser<Void, Void> {

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Ast item = obj.link;
    assert (!(item instanceof DummyLinkTarget));
    while (item instanceof Namespace) {
      ast.data.expression.reference.RefItem next = obj.offset.get(0);
      obj.offset.remove(0);
      if (!(next instanceof RefName)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName());
      }
      ast.data.expression.reference.RefName name = (ast.data.expression.reference.RefName) next;
      Ast find = NameFilter.select(((ast.data.Namespace) item).children, name.name);
      assert (item != null); // type checker should find it?
    }
    if (item instanceof RizzlyFile) {
      ast.data.expression.reference.RefItem next = obj.offset.get(0);
      obj.offset.remove(0);
      if (!(next instanceof RefName)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName());
      }
      ast.data.expression.reference.RefName name = (ast.data.expression.reference.RefName) next;
      item = Single.force(ChildCollector.select(item, new HasName(name.name)), item.getInfo());
      assert (item != null); // type checker should find it?
    }
    obj.link = (Named) item;
    return super.visitReference(obj, param);
  }

}
