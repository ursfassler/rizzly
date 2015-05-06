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

package ast.pass.eval;

import java.util.Collection;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.expression.reference.SimpleRef;
import ast.data.file.RizzlyFile;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.data.variable.TemplateParameter;
import ast.interpreter.Memory;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.pass.specializer.InstanceRepo;
import ast.pass.specializer.TypeEvaluator;
import ast.traverser.NullTraverser;

public class TemplateArgEval extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    TemplateGetter traverser = new TemplateGetter();
    traverser.traverse(ast, null);
    eval(traverser.templates, kb);
  }

  private void eval(AstList<Template> templates, KnowledgeBase kb) {
    InstanceRepo ir = new InstanceRepo();
    for (Template tmpl : templates) {
      for (TemplateParameter param : tmpl.getTempl()) {
        Type type = TypeEvaluator.evaluate(param.type, new Memory(), ir, kb);
        SimpleRef<Named> typeRef = new SimpleRef<Named>(param.type.getInfo(), type);
        param.type = typeRef;
      }
    }
  }

}

class TemplateGetter extends NullTraverser<Void, Void> {
  final public AstList<Template> templates = new AstList<Template>();

  @Override
  protected Void visitDefault(Ast obj, Void param) {
    if (obj instanceof Template) {
      templates.add((Template) obj);
    }
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitList(obj.children, param);
    return null;
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, Void param) {
    visitList(obj.objects, param);
    return null;
  }

  @Override
  protected Void visitList(Collection<? extends Ast> list, Void param) {
    for (Ast itr : list) {
      visit(itr, param);
    }
    return null;
  }

}
