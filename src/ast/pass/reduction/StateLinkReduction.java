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
import ast.data.component.hfsm.State;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefName;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.ChildByName;

/**
 * Changes references to deepest state, e.g. _top.A.B -> B
 *
 * @author urs
 *
 */
public class StateLinkReduction implements AstPass {

  @Override
  public void process(ast.data.Namespace root, KnowledgeBase kb) {
    StateLinkReductionWorker reduction = new StateLinkReductionWorker();
    reduction.traverse(root, null);
  }
}

class StateLinkReductionWorker extends DfsTraverser<Void, Void> {

  @Override
  protected Void visitOffsetReference(OffsetReference obj, Void param) {
    LinkedAnchor anchor = (LinkedAnchor) obj.getAnchor();
    Ast item = anchor.getLink();
    if (item instanceof State) {
      while (!obj.getOffset().isEmpty()) {
        ast.data.reference.RefItem next = obj.getOffset().get(0);
        obj.getOffset().remove(0);
        item = ChildByName.get(item, ((RefName) next).name, item.metadata());
        assert (item != null);
        if (!(item instanceof State)) {
          break;
        }
      }
      anchor.setLink((Named) item);
      obj.getOffset().clear();
    }
    return super.visitOffsetReference(obj, param);
  }

}
