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

package fun.traverser.spezializer;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.BaseRef;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.expression.reference.SimpleRef;
import fun.knowledge.KnowledgeBase;
import fun.other.ActualTemplateArgument;
import fun.other.FunList;
import fun.other.Named;
import fun.other.Template;
import fun.variable.ConstGlobal;

class TypeEvalReplacer extends DefTraverser<FunList<ActualTemplateArgument>, Void> {

  final private KnowledgeBase kb;

  public TypeEvalReplacer(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  @Override
  protected FunList<ActualTemplateArgument> visitList(FunList<? extends Fun> list, Void param) {
    for (Fun ast : new FunList<Fun>(list)) {
      visit(ast, param);
    }
    return null;
  }

  @Override
  protected FunList<ActualTemplateArgument> visitDeclaration(Template obj, Void param) {
    return null;
  }

  @Override
  protected FunList<ActualTemplateArgument> visitBaseRef(BaseRef obj, Void param) {
    FunList<ActualTemplateArgument> arg = super.visitBaseRef(obj, param);
    if (obj.getLink() instanceof Template) {
      RError.ass(arg != null, obj.getInfo());
      Template template = (Template) obj.getLink();
      // FIXME problem: entries only used to name templates, not instantiate them
      // TODO names by themselves should use template arguments, but then, they are templates
      if (template.getTempl().size() != arg.size()) {
        RError.err(ErrorType.Error, obj.getInfo(), "Wrong number of parameter, expected " + template.getTempl().size() + " got " + arg.size());
        return null;
      }
      Fun inst = Specializer.process(template, arg, kb);
      obj.setLink((Named) inst);
    } else if ((obj.getLink() instanceof ConstGlobal)) {
      // FIXME this is a temporary workaround, the copy should be done in the
      // instantiater (Specializer?)
      visit(obj.getLink(), null); // somebody has to instantiate the constant
    }
    return null;
  }

  @Override
  protected FunList<ActualTemplateArgument> visitSimpleRef(SimpleRef obj, Void param) {
    super.visitSimpleRef(obj, param);
    return new FunList<ActualTemplateArgument>();
  }

  @Override
  protected FunList<ActualTemplateArgument> visitReference(Reference obj, Void param) {
    super.visitReference(obj, param);
    FunList<ActualTemplateArgument> arg = new FunList<ActualTemplateArgument>();
    if (!obj.getOffset().isEmpty()) {
      if (obj.getOffset().get(0) instanceof RefTemplCall) {
        arg = ((RefTemplCall) obj.getOffset().get(0)).getActualParameter();
        obj.getOffset().remove(0);
      }
    }
    return arg;
  }
}
