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

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.reference.LinkedReferenceWithOffset;
import ast.data.reference.RefTemplCall;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.reference.ReferenceWithOffset;
import ast.data.template.ActualTemplateArgument;
import ast.data.template.Template;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowledgeBase;

public class TypeEvalExecutor extends DfsTraverser<Void, Void> {
  final private KnowledgeBase kb;

  public static void eval(Ast root, KnowledgeBase kb) {
    TypeEvalExecutor executor = new TypeEvalExecutor(kb);
    executor.traverse(root, null);
  }

  public TypeEvalExecutor(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  @Override
  protected Void visitTemplate(Template obj, Void param) {
    return null;
  }

  @Override
  protected Void visitReference(LinkedReferenceWithOffset_Implementation obj, Void param) {
    super.visitReference(obj, param);

    if (obj.getLink() instanceof Template) {
      evalRefToTempl(obj);
    }
    return null;
  }

  private void evalRefToTempl(LinkedReferenceWithOffset obj) {
    Template template = (Template) obj.getLink();
    AstList<ActualTemplateArgument> arg = extractArg(obj);
    Named evaluated = Specializer.specialize(template, arg, kb);
    obj.setLink(evaluated);
  }

  private static AstList<ActualTemplateArgument> extractArg(ReferenceWithOffset obj) {
    AstList<ActualTemplateArgument> arg = new AstList<ActualTemplateArgument>();
    if (!obj.getOffset().isEmpty()) {
      if (obj.getOffset().get(0) instanceof RefTemplCall) {
        arg = ((RefTemplCall) obj.getOffset().get(0)).actualParameter;
        obj.getOffset().remove(0);
      }
    }
    return arg;
  }

}
