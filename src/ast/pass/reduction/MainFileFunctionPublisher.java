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

package ast.pass.reduction;

import main.Configuration;
import ast.Designator;
import ast.data.Ast;
import ast.data.Namespace;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.ChildByName;
import ast.repository.query.Single;
import ast.visitor.EveryVisitor;
import error.RError;

public class MainFileFunctionPublisher extends AstPass {

  public MainFileFunctionPublisher(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    String rootFileName = configuration.getNamespace();
    ChildByName childByName = new ChildByName(new Single(RError.instance()));
    Ast rootFile = childByName.get(ast, new Designator(rootFileName), ast.metadata());
    FunctionPublisher publisher = new FunctionPublisher();
    EveryVisitor visitor = new EveryVisitor();
    visitor.addDefaultHandler(publisher);
    rootFile.accept(visitor);
  }
}
