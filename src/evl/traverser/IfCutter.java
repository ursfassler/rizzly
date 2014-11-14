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

package evl.traverser;

import evl.DefTraverser;
import evl.other.EvlList;
import evl.other.RizzlyProgram;
import evl.statement.Block;
import evl.statement.IfOption;
import evl.statement.IfStmt;

/**
 * Ensures that the if statement has at most one option
 *
 */
public class IfCutter extends DefTraverser<Void, Void> {
  static final private IfCutter INSTANCE = new IfCutter();

  public static void process(RizzlyProgram prg) {
    INSTANCE.traverse(prg, null);
  }

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
