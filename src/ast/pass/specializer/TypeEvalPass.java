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
import ast.data.Namespace;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefTemplCall;
import ast.data.template.ActualTemplateArgument;
import ast.data.template.Template;
import ast.data.variable.DefaultVariable;
import ast.dispatcher.DfsTraverser;
import ast.interpreter.Memory;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.manipulator.Manipulate;
import ast.specification.IsClass;

/**
 * Replaces all types with the evaluated expression:
 *
 * a : U{3+5} => a : U_8
 *
 * @author urs
 *
 */
public class TypeEvalPass implements AstPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    kb.clear();
    instantiateTemplateReferences(root, kb);
    Manipulate.remove(root, new IsClass(Template.class));
  }

  public static void instantiateTemplateReferences(Ast root, KnowledgeBase kb) {
    Evaluator evaluator = new Evaluator(kb);
    evaluator.traverse(root, null);
  }

  private static String full(Named obj) {
    return obj.getName() + "[" + Integer.toHexString(obj.hashCode()) + "]";
  }
}

class Evaluator extends DfsTraverser<Void, Void> {
  final private KnowledgeBase kb;

  public Evaluator(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  @Override
  protected Void visitRefTemplCall(RefTemplCall obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitOffsetReference(OffsetReference obj, Void param) {
    // TODO can we all (following) instantiate like this?

    LinkedAnchor anchor = (LinkedAnchor) obj.getAnchor();
    if (anchor.getLink() instanceof Template) {
      assert (!obj.getOffset().isEmpty() && (obj.getOffset().get(0) instanceof RefTemplCall));

      Template template = (Template) anchor.getLink();
      AstList<ActualTemplateArgument> acarg = ((RefTemplCall) obj.getOffset().get(0)).actualParameter;
      obj.getOffset().remove(0);
      anchor.setLink(Specializer.specialize(template, acarg, kb));
    }

    return super.visitOffsetReference(obj, param);
  }

  @Override
  protected Void visitDefVariable(DefaultVariable obj, Void param) {
    visit(obj.type, param);
    obj.def = ExprEvaluator.evaluate(obj.def, new Memory(), kb);
    return null;
  }

  @Override
  protected Void visitTemplate(Template obj, Void param) {
    visitList(obj.getTempl(), param);
    return null;
  }

}
