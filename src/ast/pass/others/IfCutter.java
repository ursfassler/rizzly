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
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.statement.Block;
import ast.data.statement.IfOption;
import ast.data.statement.IfStatement;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;

/**
 * Ensures that the if statement has at most one option
 *
 */
// TODO replace IfStmt with simple if stmt or remove simple if statement
public class IfCutter extends AstPass {
  public IfCutter(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    IfCutterWorker worker = new IfCutterWorker();
    worker.traverse(ast, null);
  }

}

class IfCutterWorker extends DfsTraverser<Void, Void> {

  @Override
  protected Void visitIfStmt(IfStatement obj, Void param) {
    if (obj.option.size() > 1) {
      int optcount = obj.option.size();
      IfOption first = obj.option.get(0);
      AstList<IfOption> opt = new AstList<IfOption>(obj.option);
      obj.option.clear();
      obj.option.add(first);
      opt.remove(0);
      assert (obj.option.size() + opt.size() == optcount);

      IfStatement nif = new IfStatement(opt, obj.defblock);
      nif.metadata().add(opt.get(0).metadata());
      Block newElse = new Block();
      newElse.metadata().add(obj.metadata());
      newElse.statements.add(nif);
      obj.defblock = newElse;
    }
    return super.visitIfStmt(obj, param);
  }

}
