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

package ast.pass.others;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.function.template.DefaultValueTemplate;
import ast.data.function.template.FunctionTemplate;
import ast.data.template.Template;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;

public class InternFuncAdder extends AstPass {

  @Override
  public void process(ast.data.Namespace root, KnowledgeBase kb) {
    genTemplateFunctions(root.children);
  }

  private static void genTemplateFunctions(AstList<Ast> container) {
    templ(new DefaultValueTemplate(), container);
  }

  private static void templ(FunctionTemplate tmpl, AstList<Ast> container) {
    Template decl = new Template(tmpl.getInfo(), tmpl.getName(), tmpl.makeParam(), tmpl);
    container.add(decl);
  }

}
