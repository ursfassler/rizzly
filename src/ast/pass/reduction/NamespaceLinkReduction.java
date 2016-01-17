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

import main.Configuration;
import ast.data.Ast;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.file.RizzlyFile;
import ast.data.reference.LinkTarget;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.ChildCollector;
import ast.repository.query.NameFilter;
import ast.repository.query.Single;
import ast.specification.HasName;
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
  public NamespaceLinkReduction(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    NamespaceLinkReductionWorker reduction = new NamespaceLinkReductionWorker();
    reduction.traverse(root, null);
  }

}

class NamespaceLinkReductionWorker extends DfsTraverser<Void, Void> {

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Ast item = obj.link;
    assert (!(item instanceof LinkTarget));
    while (item instanceof Namespace) {
      ast.data.reference.RefItem next = obj.offset.get(0);
      obj.offset.remove(0);
      if (!(next instanceof RefName)) {
        RError.err(ErrorType.Error, "Expected named offset, got: " + next.getClass().getCanonicalName(), obj.metadata());
      }
      ast.data.reference.RefName name = (ast.data.reference.RefName) next;
      Ast find = NameFilter.select(((ast.data.Namespace) item).children, name.name);
      assert (item != null); // type checker should find it?
    }
    if (item instanceof RizzlyFile) {
      ast.data.reference.RefItem next = obj.offset.get(0);
      obj.offset.remove(0);
      if (!(next instanceof RefName)) {
        RError.err(ErrorType.Error, "Expected named offset, got: " + next.getClass().getCanonicalName(), obj.metadata());
      }
      ast.data.reference.RefName name = (ast.data.reference.RefName) next;
      item = Single.staticForce(ChildCollector.select(item, new HasName(name.name)), item.metadata());
      assert (item != null); // type checker should find it?
    }
    obj.link = (Named) item;
    return super.visitReference(obj, param);
  }

}
