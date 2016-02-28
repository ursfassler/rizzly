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

import ast.data.AstList;
import ast.data.Namespace;
import ast.data.reference.Anchor;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefItem;
import ast.data.reference.RefTemplCall;
import ast.data.reference.Reference;
import ast.data.template.ActualTemplateArgument;
import ast.data.template.Template;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.Collector;
import ast.specification.IsClass;
import error.ErrorType;
import error.RError;

public class TemplCallAdder implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    for (Reference ref : listOfReferences(ast)) {
      if (isTemplateCall(ref.getAnchor()) && missingCall((OffsetReference) ref)) {
        insertTemplateCall((OffsetReference) ref);
      }
    }
  }

  private AstList<Reference> listOfReferences(Namespace ast) {
    return Collector.select(ast, new IsClass(Reference.class)).castTo(Reference.class);
  }

  private boolean isTemplateCall(Anchor ref) {
    return ((LinkedAnchor) ref).getLink() instanceof Template;
  }

  private boolean missingCall(OffsetReference ref) {
    AstList<RefItem> offset = ref.getOffset();
    return offset.isEmpty() || !(offset.get(0) instanceof RefTemplCall);
  }

  private void insertTemplateCall(OffsetReference ref) {
    LinkedAnchor anchor = ((LinkedAnchor) ref.getAnchor());
    if (!((Template) anchor.getLink()).getTempl().isEmpty()) {
      RError.err(ErrorType.Error, "Missing template argument", ref.metadata());
    }
    ref.getOffset().add(0, new RefTemplCall(new AstList<ActualTemplateArgument>()));
  }
}
