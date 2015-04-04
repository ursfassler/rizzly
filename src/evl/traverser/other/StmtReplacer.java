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

package evl.traverser.other;

import java.util.ArrayList;
import java.util.List;

import evl.data.EvlList;
import evl.data.statement.Block;
import evl.data.statement.Statement;
import evl.traverser.DefTraverser;

public class StmtReplacer<T> extends DefTraverser<List<Statement>, T> {

  protected List<Statement> list(Statement stmt1) {
    List<Statement> ret = new ArrayList<Statement>(1);
    ret.add(stmt1);
    return ret;
  }

  protected List<Statement> list(Statement stmt1, Statement stmt2) {
    List<Statement> ret = new ArrayList<Statement>(1);
    ret.add(stmt1);
    ret.add(stmt2);
    return ret;
  }

  protected List<Statement> list(Statement stmt1, Statement stmt2, Statement stmt3) {
    List<Statement> ret = new ArrayList<Statement>(1);
    ret.add(stmt1);
    ret.add(stmt2);
    ret.add(stmt3);
    return ret;
  }

  @Override
  protected List<Statement> visitBlock(Block obj, T param) {
    EvlList<Statement> oldStmt = new EvlList<Statement>(obj.statements);
    obj.statements.clear();

    for (Statement stmt : oldStmt) {
      List<Statement> retStmt = visit(stmt, param);
      if (retStmt == null) {
        obj.statements.add(stmt);
      } else {
        obj.statements.addAll(retStmt);
      }
    }
    return null;
  }

}
