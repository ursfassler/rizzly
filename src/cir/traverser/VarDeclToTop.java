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

package cir.traverser;

import java.util.LinkedList;

import cir.CirBase;
import cir.DefTraverser;
import cir.statement.Block;
import cir.statement.Statement;
import cir.statement.VarDefStmt;

public class VarDeclToTop extends DefTraverser<Void, Void> {

  public static void process(CirBase cprog) {
    VarDeclToTop cVarDeclToTop = new VarDeclToTop();
    cVarDeclToTop.traverse(cprog, null);
  }

  @Override
  protected Void visitBlock(Block obj, Void param) {
    LinkedList<Statement> nlist = new LinkedList<Statement>();
    for (Statement itr : obj.getStatement()) {
      visit(itr, param);
      if (itr instanceof VarDefStmt) {
        nlist.addFirst(itr);
      } else {
        nlist.addLast(itr);
      }
    }
    obj.getStatement().clear();
    obj.getStatement().addAll(nlist);
    return null;
  }

}
