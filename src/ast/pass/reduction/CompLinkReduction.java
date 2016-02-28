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
import ast.data.component.composition.ComponentUse;
import ast.data.file.RizzlyFile;
import ast.data.raw.RawComponent;
import ast.data.raw.RawComposition;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefTemplCall;
import ast.dispatcher.NullDispatcher;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.NameFilter;
import ast.repository.query.TypeFilter;
import error.ErrorType;
import error.RError;

/**
 * Changes references to components, e.g. comp.foo.Bar -> Bar
 *
 * @author urs
 *
 */
public class CompLinkReduction implements AstPass {

  @Override
  public void process(ast.data.Namespace root, KnowledgeBase kb) {
    CompLinkReductionWorker reduction = new CompLinkReductionWorker();
    reduction.traverse(root, null);
  }

}

class CompLinkReductionWorker extends NullDispatcher<Void, Void> {

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
  protected Void visitCompUse(ComponentUse obj, Void param) {
    OffsetReference compRef = (OffsetReference) obj.compRef;
    LinkedAnchor anchor = (LinkedAnchor) compRef.getAnchor();
    Named item = anchor.getLink();

    while (!compRef.getOffset().isEmpty()) {
      if (compRef.getOffset().get(0) instanceof RefTemplCall) {
        break;
      }
      ast.data.reference.RefName rn = (ast.data.reference.RefName) compRef.getOffset().get(0);
      compRef.getOffset().remove(0);
      if (item instanceof RizzlyFile) {
        item = NameFilter.select(TypeFilter.select(((RizzlyFile) item).objects, RawComponent.class), rn.name);
      } else if (item instanceof Namespace) {
        item = (Named) NameFilter.select(((ast.data.Namespace) item).children, rn.name);
      } else {
        RError.err(ErrorType.Fatal, "Unhandled type: " + item.getClass().getCanonicalName(), item.metadata());
      }
      assert (item != null);
    }

    assert (item instanceof RawComponent);
    anchor.setLink(item);

    return null;
  }

}
