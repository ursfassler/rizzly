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

package fun.pass;

import pass.FunPass;

import common.ElementInfo;

import fun.DefTraverser;
import fun.expression.AnyValue;
import fun.expression.reference.Reference;
import fun.knowledge.KnowledgeBase;
import fun.other.FunList;
import fun.other.Namespace;
import fun.statement.Assignment;
import fun.statement.Block;
import fun.statement.Statement;
import fun.statement.VarDefStmt;
import fun.variable.FuncVariable;

/**
 * Splits variable definitions such that only 1 variable is defined per variable definition statement. If the variable
 * has an assignment, that one is also extracted to its own statement.
 *
 * @author urs
 *
 */
public class VarDefSplitter extends FunPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    VarDefSplitterWorker worker = new VarDefSplitterWorker();
    worker.traverse(root, null);
  }

}

class VarDefSplitterWorker extends DefTraverser<FunList<Statement>, Void> {

  @Override
  protected FunList<Statement> visitBlock(Block obj, Void param) {
    FunList<Statement> list = new FunList<Statement>();
    for (Statement stmt : obj.getStatements()) {
      FunList<Statement> ret = visit(stmt, param);
      if (ret == null) {
        list.add(stmt);
      } else {
        list.addAll(ret);
      }
    }
    obj.getStatements().clear();
    obj.getStatements().addAll(list);
    return null;
  }

  @Override
  protected FunList<Statement> visitVarDefStmt(VarDefStmt obj, Void param) {
    FunList<Statement> ret = new FunList<Statement>();

    FuncVariable firstVar = null;

    for (FuncVariable var : obj.getVariable()) {
      ElementInfo info = var.getInfo();

      // variable definition
      FunList<FuncVariable> sl = new FunList<FuncVariable>();
      sl.add(var);
      ret.add(new VarDefStmt(info, sl, new AnyValue(info)));

      // assign initial value
      if (!(obj.getInitial() instanceof AnyValue)) {
        if (firstVar == null) {
          FunList<Reference> al = new FunList<Reference>();
          al.add(new Reference(info, var));
          ret.add(new Assignment(var.getInfo(), al, obj.getInitial()));
          firstVar = var;
        } else {
          FunList<Reference> al = new FunList<Reference>();
          al.add(new Reference(info, var));
          ret.add(new Assignment(var.getInfo(), al, new Reference(info, firstVar)));
        }
      }
    }

    return ret;
  }

}
