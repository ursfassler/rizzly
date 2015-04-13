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

package fun.pass;

import pass.EvlPass;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.function.template.DefaultValueTemplate;
import evl.data.function.template.FunctionTemplate;
import evl.knowledge.KnowledgeBase;
import fun.other.Template;

public class InternFuncAdder extends EvlPass {

  @Override
  public void process(evl.data.Namespace root, KnowledgeBase kb) {
    genTemplateFunctions(root.children);
  }

  private static void genTemplateFunctions(EvlList<Evl> container) {
    templ(new DefaultValueTemplate(), container);
  }

  private static void templ(FunctionTemplate tmpl, EvlList<Evl> container) {
    Template decl = new Template(tmpl.getInfo(), tmpl.getName(), tmpl.makeParam(), tmpl);
    container.add(decl);
  }

}
