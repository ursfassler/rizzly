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
import ast.repository.query.Collector;
import ast.specification.IsClass;
import error.ErrorType;
import error.RError;

class TypeEvalExecutor {

  public static void eval(Ast root, InstanceRepo ir, KnowledgeBase kb) {
    AstList<Reference> list = Collector.select(root, new IsClass(Reference.class)).castTo(Reference.class);

    for (Reference itr : list) {
      if (itr.link instanceof Template) {
        evalTemplate(itr, ir, kb);
      }
    }
  }

  private static void evalTemplate(Reference obj, InstanceRepo ir, KnowledgeBase kb) {
    AstList<ActualTemplateArgument> arg = new AstList<ActualTemplateArgument>();
    if (!obj.offset.isEmpty()) {
      if (obj.offset.get(0) instanceof RefTemplCall) {
        arg = ((RefTemplCall) obj.offset.get(0)).actualParameter;
        obj.offset.remove(0);
      }
    }

    Template template = (Template) obj.link;
    if (template.getTempl().size() != arg.size()) {
      RError.err(ErrorType.Error, obj.getInfo(), "Wrong number of parameter, expected " + template.getTempl().size() + " got " + arg.size());
      return;
    }

    Ast inst = Specializer.process(template, arg, ir, kb);
    obj.link = (Named) inst;
  }

}
