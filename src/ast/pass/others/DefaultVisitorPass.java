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
import ast.visitor.DeepFirstTraverser;
import ast.visitor.VisitExecutorImplementation;
import ast.visitor.Visitor;

public class DefaultVisitorPass extends AstPass {
  final private Visitor visitor;

  public DefaultVisitorPass(Visitor visitor, Configuration configuration) {
    super(configuration);
    this.visitor = visitor;
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    DeepFirstTraverser allVisitor = new DeepFirstTraverser();
    allVisitor.addPreorderVisitor(visitor);
    (new VisitExecutorImplementation()).visit(allVisitor, ast);
  }

}