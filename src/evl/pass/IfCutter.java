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

import pass.EvlPass;
import evl.DefTraverser;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Namespace;
import evl.statement.Block;
import evl.statement.IfOption;
import evl.statement.IfStmt;

/**
 * Ensures that the if statement has at most one option
 *
 */
// TODO replace IfStmt with simple if stmt or remove simple if statement
public class IfCutter extends EvlPass {
  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    IfCutterWorker worker = new IfCutterWorker();
    worker.traverse(evl, null);
  }

}

class IfCutterWorker extends DefTraverser<Void, Void> {

  @Override
  protected Void visitIfStmt(IfStmt obj, Void param) {
    if (obj.getOption().size() > 1) {
      int optcount = obj.getOption().size();
      IfOption first = obj.getOption().get(0);
      EvlList<IfOption> opt = new EvlList<IfOption>(obj.getOption());
      obj.getOption().clear();
      obj.getOption().add(first);
      opt.remove(0);
      assert (obj.getOption().size() + opt.size() == optcount);

      IfStmt nif = new IfStmt(opt.get(0).getInfo(), opt, obj.getDefblock());
      Block newElse = new Block(obj.getInfo());
      newElse.getStatements().add(nif);
      obj.setDefblock(newElse);
    }
    return super.visitIfStmt(obj, param);
  }

}
