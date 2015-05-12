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

package ast.pass.optimize;

import java.util.ArrayList;
import java.util.List;

import ast.data.Namespace;
import ast.data.statement.Block;
import ast.data.statement.Statement;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;

public class BlockReduction extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    BlockReductionWorker reduction = new BlockReductionWorker();
    reduction.traverse(ast, null);
  }

}

class BlockReductionWorker extends DfsTraverser<Void, Void> {

  @Override
  protected Void visitBlock(Block obj, Void param) {
    List<Statement> old = new ArrayList<Statement>(obj.statements);
    obj.statements.clear();

    for (Statement stmt : old) {
      visitStatement(stmt, null);
      if (stmt instanceof Block) {
        obj.statements.addAll(((Block) stmt).statements);
      } else {
        obj.statements.add(stmt);
      }
    }

    return null;
  }

}
