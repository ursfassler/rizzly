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

package ast.pass;

import java.util.LinkedList;

import pass.AstPass;
import ast.data.Namespace;
import ast.data.statement.Block;
import ast.data.statement.Statement;
import ast.data.statement.VarDefStmt;
import ast.knowledge.KnowledgeBase;
import ast.traverser.DefTraverser;

public class VarDeclToTop extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    VarDeclToTopWorker cVarDeclToTop = new VarDeclToTopWorker();
    cVarDeclToTop.traverse(ast, null);
  }

}

class VarDeclToTopWorker extends DefTraverser<Void, Void> {

  @Override
  protected Void visitBlock(Block obj, Void param) {
    LinkedList<Statement> nlist = new LinkedList<Statement>();
    for (Statement itr : obj.statements) {
      visit(itr, param);
      if (itr instanceof VarDefStmt) {
        nlist.addFirst(itr);
      } else {
        nlist.addLast(itr);
      }
    }
    obj.statements.clear();
    obj.statements.addAll(nlist);
    return null;
  }

}
