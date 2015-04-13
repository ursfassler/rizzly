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
import ast.data.expression.reference.RefTemplCall;
import ast.data.expression.reference.Reference;
import ast.data.file.RizzlyFile;
import ast.data.raw.RawComponent;
import ast.data.raw.RawComposition;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.traverser.NullTraverser;
import ast.traverser.other.ClassGetter;
import error.ErrorType;
import error.RError;

/**
 * Changes references to components, e.g. comp.foo.Bar -> Bar
 *
 * @author urs
 *
 */
public class CompLinkReduction extends AstPass {

  @Override
  public void process(ast.data.Namespace root, KnowledgeBase kb) {
    CompLinkReductionWorker reduction = new CompLinkReductionWorker();
    reduction.traverse(root, null);
  }

}

class CompLinkReductionWorker extends NullTraverser<Void, Void> {

  @Override
  protected Void visitDefault(Ast obj, Void param) {
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitList(obj.children, param);
    return null;
  }

  @Override
  protected Void visitRawComposition(RawComposition obj, Void param) {
    visitList(obj.getInstantiation(), param);
    return null;
  }

  @Override
  protected Void visitCompUse(ast.data.component.composition.CompUse obj, Void param) {
    Reference compRef = (Reference) obj.compRef;
    Named item = compRef.link;

    while (!compRef.offset.isEmpty()) {
      if (compRef.offset.get(0) instanceof RefTemplCall) {
        break;
      }
      ast.data.expression.reference.RefName rn = (ast.data.expression.reference.RefName) compRef.offset.get(0);
      compRef.offset.remove(0);
      if (item instanceof RizzlyFile) {
        item = ClassGetter.filter(RawComponent.class, ((RizzlyFile) item).getObjects()).find(rn.name);
      } else if (item instanceof Namespace) {
        item = (Named) ((ast.data.Namespace) item).children.find(rn.name);
      } else {
        RError.err(ErrorType.Fatal, item.getInfo(), "Unhandled type: " + item.getClass().getCanonicalName());
      }
      assert (item != null);
    }

    assert (item instanceof RawComponent);
    compRef.link = item;

    return null;
  }

}
