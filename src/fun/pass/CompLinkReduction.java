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
import fun.Fun;
import fun.NullTraverser;
import fun.composition.ImplComposition;
import fun.expression.reference.RefName;
import fun.expression.reference.RefTemplCall;
import fun.knowledge.KnowledgeBase;
import fun.other.CompImpl;
import fun.other.Named;
import fun.other.Namespace;
import fun.other.RizzlyFile;
import fun.variable.CompUse;

/**
 * Changes references to components, e.g. comp.foo.Bar -> Bar
 *
 * @author urs
 *
 */
public class CompLinkReduction extends FunPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    CompLinkReductionWorker reduction = new CompLinkReductionWorker();
    reduction.traverse(root, null);
  }

}

class CompLinkReductionWorker extends NullTraverser<Void, Void> {

  @Override
  protected Void visitDefault(Fun obj, Void param) {
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitList(obj.getChildren(), param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    visitList(obj.getInstantiation().getItems(CompImpl.class), param);
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, Void param) {
    Named item = obj.getType().getLink();

    while (!obj.getType().getOffset().isEmpty()) {
      if (obj.getType().getOffset().get(0) instanceof RefTemplCall) {
        break;
      }
      RefName rn = (RefName) obj.getType().getOffset().get(0);
      obj.getType().getOffset().remove(0);
      if (item instanceof RizzlyFile) {
        item = ((RizzlyFile) item).getObjects().getItems(CompImpl.class).find(rn.getName());
      } else if (item instanceof Namespace) {
        item = ((Namespace) item).getChildren().find(rn.getName());
      } else {
        RError.err(ErrorType.Fatal, item.getInfo(), "Unhandled type: " + item.getClass().getCanonicalName());
      }
      assert (item != null);
    }

    assert (item instanceof CompImpl);
    obj.getType().setLink(item);

    return null;
  }

}
