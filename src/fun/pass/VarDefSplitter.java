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

import pass.EvlPass;

import common.ElementInfo;

import evl.data.EvlList;
import evl.data.expression.AnyValue;
import evl.data.expression.reference.Reference;
import evl.data.statement.AssignmentMulti;
import evl.data.statement.Block;
import evl.data.statement.Statement;
import evl.data.statement.VarDefInitStmt;
import evl.data.variable.FuncVariable;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;

/**
 * Splits variable definitions such that only 1 variable is defined per variable definition statement. If the variable
 * has an assignment, that one is also extracted to its own statement.
 *
 * @author urs
 *
 */
public class VarDefSplitter extends EvlPass {

  @Override
  public void process(evl.data.Namespace root, KnowledgeBase kb) {
    VarDefSplitterWorker worker = new VarDefSplitterWorker();
    worker.traverse(root, null);
  }

}

class VarDefSplitterWorker extends DefTraverser<EvlList<Statement>, Void> {

  @Override
  protected EvlList<Statement> visitBlock(Block obj, Void param) {
    EvlList<Statement> list = new EvlList<Statement>();
    for (Statement stmt : obj.statements) {
      EvlList<Statement> ret = visit(stmt, param);
      if (ret == null) {
        list.add(stmt);
      } else {
        list.addAll(ret);
      }
    }
    obj.statements.clear();
    obj.statements.addAll(list);
    return null;
  }

  @Override
  protected EvlList<Statement> visitVarDefInitStmt(VarDefInitStmt obj, Void param) {
    EvlList<Statement> ret = new EvlList<Statement>();

    evl.data.variable.FuncVariable firstVar = null;

    for (FuncVariable var : obj.variable) {
      ElementInfo info = var.getInfo();

      // variable definition
      EvlList<FuncVariable> sl = new EvlList<FuncVariable>();
      sl.add(var);
      ret.add(new VarDefInitStmt(info, sl, new AnyValue(info)));

      // assign initial value
      if (!(obj.initial instanceof AnyValue)) {
        if (firstVar == null) {
          EvlList<Reference> al = new EvlList<Reference>();
          al.add(new Reference(info, var));
          ret.add(new AssignmentMulti(var.getInfo(), al, obj.initial));
          firstVar = var;
        } else {
          EvlList<Reference> al = new EvlList<Reference>();
          al.add(new Reference(info, var));
          ret.add(new AssignmentMulti(var.getInfo(), al, new Reference(info, firstVar)));
        }
      }
    }

    return ret;
  }

}
