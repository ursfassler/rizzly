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

import main.Configuration;
import ast.data.AstList;
import ast.data.Namespace;
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

public class TemplCallAdder extends AstPass {
  public TemplCallAdder(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    AstList<Reference> refList = listOfReferences(ast);
    for (Reference ref : refList) {
      if (isTemplateCall(ref) && missingCall(ref)) {
        insertTemplateCall(ref);
      }
    }
  }

  private boolean isTemplateCall(Reference ref) {
    return ref.link instanceof Template;
  }

  private boolean missingCall(Reference ref) {
    return ref.offset.isEmpty() || !(ref.offset.get(0) instanceof RefTemplCall);
  }

  private void insertTemplateCall(Reference ref) {
    if (!((Template) ref.link).getTempl().isEmpty()) {
      RError.err(ErrorType.Error, "Missing template argument", ref.metadata());
    }
    ref.offset.add(0, new RefTemplCall(new AstList<ActualTemplateArgument>()));
  }

  private AstList<Reference> listOfReferences(Namespace ast) {
    AstList<Reference> refList = Collector.select(ast, new IsClass(Reference.class)).castTo(Reference.class);
    return refList;
  }

}
