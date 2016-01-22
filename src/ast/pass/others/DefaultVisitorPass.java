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

import main.Configuration;
import ast.data.Namespace;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.visitor.DefaultHandler;
import ast.visitor.EveryVisitor;

public class DefaultVisitorPass extends AstPass {
  final private DefaultHandler handler;

  public DefaultVisitorPass(DefaultHandler handler, Configuration configuration) {
    super(configuration);
    this.handler = handler;
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    EveryVisitor visitor = new EveryVisitor();
    visitor.addDefaultHandler(handler);
    ast.accept(visitor);
  }

}
