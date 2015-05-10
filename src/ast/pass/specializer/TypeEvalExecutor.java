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
import ast.data.reference.RefTemplCall;
import ast.data.reference.Reference;
import ast.data.template.ActualTemplateArgument;
import ast.data.template.Template;
import ast.knowledge.KnowledgeBase;
import ast.traverser.DefTraverser;
import error.ErrorType;
import error.RError;

public class TypeEvalExecutor extends DefTraverser<Void, Void> {
  final private InstanceRepo ir;
  final private KnowledgeBase kb;

  public static void eval(Ast root, InstanceRepo ir, KnowledgeBase kb) {
    TypeEvalExecutor executor = new TypeEvalExecutor(ir, kb);
    executor.traverse(root, null);
  }

  public TypeEvalExecutor(InstanceRepo ir, KnowledgeBase kb) {
    super();
    this.ir = ir;
    this.kb = kb;
  }

  @Override
  protected Void visitTemplate(Template obj, Void param) {
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    super.visitReference(obj, param);

    AstList<ActualTemplateArgument> arg = new AstList<ActualTemplateArgument>();
    if (!obj.offset.isEmpty()) {
      if (obj.offset.get(0) instanceof RefTemplCall) {
        arg = ((RefTemplCall) obj.offset.get(0)).actualParameter;
        obj.offset.remove(0);
      }
    }

    if (obj.link instanceof Template) {
      RError.ass(arg != null, obj.getInfo());
      Template template = (Template) obj.link;
      if (template.getTempl().size() != arg.size()) {
        RError.err(ErrorType.Error, obj.getInfo(), "Wrong number of parameter, expected " + template.getTempl().size() + " got " + arg.size());
        return null;
      }
      Ast inst = Specializer.evalArgAndProcess(template, arg, ir, kb);
      obj.link = (Named) inst;
    }
    return null;
  }

}
