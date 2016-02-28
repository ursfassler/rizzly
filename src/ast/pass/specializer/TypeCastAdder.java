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

package ast.pass.specializer;

import ast.data.Named;
import ast.data.Namespace;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.TypeCast;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefCall;
import ast.data.reference.RefTemplCall;
import ast.data.reference.Reference;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.dispatcher.other.ExprReplacer;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;

public class TypeCastAdder implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    TypeCastAdderWorker worker = new TypeCastAdderWorker();
    worker.traverse(ast, null);
  }

}

class TypeCastAdderWorker extends ExprReplacer<Void> {

  @Override
  protected Expression visitRefExpr(ReferenceExpression obj, Void param) {
    OffsetReference reference = (OffsetReference) obj.reference;
    visit(reference, param);

    if (isTypeCast(reference)) {
      assert (reference.getOffset().size() == 2);
      assert (reference.getOffset().get(0) instanceof RefTemplCall);
      assert (reference.getOffset().get(1) instanceof RefCall);
      assert (((RefCall) reference.getOffset().get(1)).actualParameter.value.size() == 1);

      Expression value = ((RefCall) reference.getOffset().get(1)).actualParameter.value.get(0);
      reference.getOffset().remove(1);

      Reference typeRef = reference;
      typeRef.metadata().add(obj.metadata());
      TypeCast typeCast = new TypeCast(typeRef, value);
      typeCast.metadata().add(obj.metadata());
      return typeCast;
    }

    return obj;
  }

  private boolean isTypeCast(OffsetReference ref) {
    Named link = ((LinkedAnchor) ref.getAnchor()).getLink();
    if ((link instanceof Type) && (ref.getOffset().size() >= 1)) {
      return true;
    }
    if ((link instanceof Template) && (ref.getOffset().size() >= 2) && (ref.getOffset().get(1) instanceof RefCall)) {
      Template template = (Template) link;
      if (template.getObject() instanceof Type) {
        return true;
      }
    }

    return false;
  }

}
