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
import ast.data.expression.AnyValue;
import ast.data.expression.reference.Reference;
import ast.data.statement.AssignmentMulti;
import ast.data.statement.Block;
import ast.data.statement.Statement;
import ast.data.statement.VarDefInitStmt;
import ast.data.variable.FuncVariable;
import ast.knowledge.KnowledgeBase;
import ast.traverser.DefTraverser;

import common.ElementInfo;

/**
 * Splits variable definitions such that only 1 variable is defined per variable definition statement. If the variable
 * has an assignment, that one is also extracted to its own statement.
 *
 * @author urs
 *
 */
public class VarDefSplitter extends AstPass {

  @Override
  public void process(ast.data.Namespace root, KnowledgeBase kb) {
    VarDefSplitterWorker worker = new VarDefSplitterWorker();
    worker.traverse(root, null);
  }

}

class VarDefSplitterWorker extends DefTraverser<AstList<Statement>, Void> {

  @Override
  protected AstList<Statement> visitBlock(Block obj, Void param) {
    AstList<Statement> list = new AstList<Statement>();
    for (Statement stmt : obj.statements) {
      AstList<Statement> ret = visit(stmt, param);
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
  protected AstList<Statement> visitVarDefInitStmt(VarDefInitStmt obj, Void param) {
    AstList<Statement> ret = new AstList<Statement>();

    ast.data.variable.FuncVariable firstVar = null;

    for (FuncVariable var : obj.variable) {
      ElementInfo info = var.getInfo();

      // variable definition
      AstList<FuncVariable> sl = new AstList<FuncVariable>();
      sl.add(var);
      ret.add(new VarDefInitStmt(info, sl, new AnyValue(info)));

      // assign initial value
      if (!(obj.initial instanceof AnyValue)) {
        if (firstVar == null) {
          AstList<Reference> al = new AstList<Reference>();
          al.add(new Reference(info, var));
          ret.add(new AssignmentMulti(var.getInfo(), al, obj.initial));
          firstVar = var;
        } else {
          AstList<Reference> al = new AstList<Reference>();
          al.add(new Reference(info, var));
          ret.add(new AssignmentMulti(var.getInfo(), al, new Reference(info, firstVar)));
        }
      }
    }

    return ret;
  }

}
