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
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.ChildByName;
import error.ErrorType;
import error.RError;

/**
 * Changes references to enums, e.g. Weekday.Tuesday -> Tuesday
 *
 * @author urs
 *
 */
public class EnumLinkReduction extends AstPass {

  @Override
  public void process(ast.data.Namespace root, KnowledgeBase kb) {
    EnumLinkReductionWorker reduction = new EnumLinkReductionWorker();
    reduction.traverse(root, null);
  }

}

class EnumLinkReductionWorker extends DfsTraverser<Void, Void> {

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Ast item = obj.link;
    if (item instanceof EnumType) {
      if (!obj.offset.isEmpty()) {
        ast.data.reference.RefItem next = obj.offset.get(0);
        obj.offset.remove(0);
        if (!(next instanceof RefName)) {
          RError.err(ErrorType.Error, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName());
        }
        Ast elem = ChildByName.find(item, ((ast.data.reference.RefName) next).name);
        if (elem == null) {
          RError.err(ErrorType.Error, obj.getInfo(), "Element not found: " + ((ast.data.reference.RefName) next).name);
        }
        if (elem instanceof EnumElement) {
          obj.link = (EnumElement) elem;
        } else {
          RError.err(ErrorType.Error, obj.getInfo(), "Expected enumerator element, got: " + elem.getClass().getCanonicalName());
        }
      }
    }
    return super.visitReference(obj, param);
  }
}
