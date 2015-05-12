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

package ast.pass.check.sanity;

import java.util.HashSet;
import java.util.Set;

import ast.data.Ast;
import ast.data.Namespace;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import error.ErrorType;
import error.RError;

/**
 * Checks that a object has only one parent
 *
 * @author urs
 *
 */
public class SingleDefinition extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    SingleDefinitionWorker worker = new SingleDefinitionWorker();
    worker.traverse(ast, new HashSet<Ast>());
  }
}

class SingleDefinitionWorker extends DfsTraverser<Void, Set<Ast>> {

  @Override
  protected Void visit(Ast obj, Set<Ast> param) {
    if (param.contains(obj)) {
      for (Object itr : param.toArray()) {
        if (obj == itr) {
          RError.err(ErrorType.Fatal, obj.getInfo(), "object defined at 2 places: " + obj);
        }
      }
    }
    param.add(obj);
    return super.visit(obj, param);
  }
}
