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

package ast.pass.instantiation;

import ast.data.Ast;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.expression.reference.RefItem;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.Reference;
import ast.knowledge.KnowChild;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
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
public class LinkReduction extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    // FIXME hacky
    KnowChild kc = kb.getEntry(KnowChild.class);
    Namespace inst = (Namespace) kc.find(ast, "!inst");

    LinkReductionWorker reduction = new LinkReductionWorker();
    reduction.traverse(inst, null);
  }
}

class LinkReductionWorker extends DefTraverser<Void, Void> {

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Ast item = obj.link;
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
