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

import pass.AstPass;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.statement.Block;
import ast.data.statement.IfOption;
import ast.data.statement.IfStmt;
import ast.knowledge.KnowledgeBase;
import ast.traverser.DefTraverser;

/**
 * Ensures that the if statement has at most one option
 *
 */
// TODO replace IfStmt with simple if stmt or remove simple if statement
public class IfCutter extends AstPass {
  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    IfCutterWorker worker = new IfCutterWorker();
    worker.traverse(ast, null);
  }

}

class IfCutterWorker extends DefTraverser<Void, Void> {

  @Override
  protected Void visitIfStmt(IfStmt obj, Void param) {
    if (obj.option.size() > 1) {
      int optcount = obj.option.size();
      IfOption first = obj.option.get(0);
      AstList<IfOption> opt = new AstList<IfOption>(obj.option);
      obj.option.clear();
      obj.option.add(first);
      opt.remove(0);
      assert (obj.option.size() + opt.size() == optcount);

      IfStmt nif = new IfStmt(opt.get(0).getInfo(), opt, obj.defblock);
      Block newElse = new Block(obj.getInfo());
      newElse.statements.add(nif);
      obj.defblock = newElse;
    }
    return super.visitIfStmt(obj, param);
  }

}
