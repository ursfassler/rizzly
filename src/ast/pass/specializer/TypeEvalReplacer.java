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

import java.util.Collection;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.reference.RefTemplCall;
import ast.data.reference.Reference;
import ast.data.template.ActualTemplateArgument;
import ast.data.template.Template;
import ast.data.variable.ConstGlobal;
import ast.knowledge.KnowledgeBase;
import ast.traverser.DefTraverser;
import error.ErrorType;
import error.RError;

class TypeEvalReplacer extends DefTraverser<AstList<ActualTemplateArgument>, Void> {
  final private InstanceRepo ir;
  final private KnowledgeBase kb;

  public TypeEvalReplacer(InstanceRepo ir, KnowledgeBase kb) {
    super();
    this.ir = ir;
    this.kb = kb;
  }

  @Override
  protected AstList<ActualTemplateArgument> visitList(Collection<? extends Ast> list, Void param) {
    for (Ast ast : new AstList<Ast>(list)) {
      visit(ast, param);
    }
    return null;
  }

  @Override
  protected AstList<ActualTemplateArgument> visitTemplate(Template obj, Void param) {
    return null;
  }

  @Override
  protected AstList<ActualTemplateArgument> visitReference(Reference obj, Void param) {
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
      // FIXME problem: entries only used to name templates, not instantiate
      // them
      // TODO names by themselves should use template arguments, but then, they
      // are templates
      if (template.getTempl().size() != arg.size()) {
        RError.err(ErrorType.Error, obj.getInfo(), "Wrong number of parameter, expected " + template.getTempl().size() + " got " + arg.size());
        return null;
      }
      Ast inst = Specializer.process(template, arg, ir, kb);
      obj.link = (Named) inst;
    } else if ((obj.link instanceof ConstGlobal)) {
      // FIXME this is a temporary workaround, the copy should be done in the
      // instantiater (Specializer?)
      visit(obj.link, null); // somebody has to instantiate the constant
    }
    return null;
  }

}
