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

package evl.pass;

import java.util.ArrayList;
import java.util.List;

import pass.EvlPass;
import evl.DefTraverser;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.statement.Block;
import evl.statement.Statement;

public class BlockReduction extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    BlockReductionWorker reduction = new BlockReductionWorker();
    reduction.traverse(evl, null);
  }

}

class BlockReductionWorker extends DefTraverser<Void, Void> {

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
