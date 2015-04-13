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
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.expression.reference.BaseRef;
import evl.data.expression.reference.RefTemplCall;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.variable.ConstGlobal;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;
import fun.other.ActualTemplateArgument;
import fun.other.Template;

class TypeEvalReplacer extends DefTraverser<EvlList<ActualTemplateArgument>, Void> {

  final private KnowledgeBase kb;

  public TypeEvalReplacer(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  @Override
  protected EvlList<ActualTemplateArgument> visitList(EvlList<? extends Evl> list, Void param) {
    for (Evl ast : new EvlList<Evl>(list)) {
      visit(ast, param);
    }
    return null;
  }

  @Override
  protected EvlList<ActualTemplateArgument> visitTemplate(Template obj, Void param) {
    return null;
  }

  @Override
  protected EvlList<ActualTemplateArgument> visitBaseRef(BaseRef obj, Void param) {
    EvlList<ActualTemplateArgument> arg = super.visitBaseRef(obj, param);
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
      Evl inst = Specializer.process(template, arg, kb);
      obj.link = (Named) inst;
    } else if ((obj.link instanceof ConstGlobal)) {
      // FIXME this is a temporary workaround, the copy should be done in the
      // instantiater (Specializer?)
      visit(obj.link, null); // somebody has to instantiate the constant
    }
    return null;
  }

  @Override
  protected EvlList<ActualTemplateArgument> visitSimpleRef(SimpleRef obj, Void param) {
    super.visitSimpleRef(obj, param);
    return new EvlList<ActualTemplateArgument>();
  }

  @Override
  protected EvlList<ActualTemplateArgument> visitReference(Reference obj, Void param) {
    super.visitReference(obj, param);
    EvlList<ActualTemplateArgument> arg = new EvlList<ActualTemplateArgument>();
    if (!obj.offset.isEmpty()) {
      if (obj.offset.get(0) instanceof RefTemplCall) {
        arg = ((RefTemplCall) obj.offset.get(0)).actualParameter;
        obj.offset.remove(0);
      }
    }
    return arg;
  }
}
