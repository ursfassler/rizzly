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
import ast.data.expression.ReferenceExpression;
import ast.data.expression.value.AnyValue;
import ast.data.reference.RefFactory;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.statement.Block;
import ast.data.statement.MultiAssignment;
import ast.data.statement.Statement;
import ast.data.statement.VarDefInitStmt;
import ast.data.variable.FunctionVariable;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowledgeBase;
import ast.meta.MetaList;
import ast.pass.AstPass;

/**
 * Splits variable definitions such that only 1 variable is defined per variable definition statement. If the variable
 * has an assignment, that one is also extracted to its own statement.
 *
 * @author urs
 *
 */
public class VarDefSplitter extends AstPass {
  public VarDefSplitter(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(ast.data.Namespace root, KnowledgeBase kb) {
    VarDefSplitterWorker worker = new VarDefSplitterWorker();
    worker.traverse(root, null);
  }

}

class VarDefSplitterWorker extends DfsTraverser<AstList<Statement>, Void> {

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

    ast.data.variable.FunctionVariable firstVar = null;

    for (FunctionVariable var : obj.variable) {
      MetaList info = var.metadata();

      // variable definition
      AstList<FunctionVariable> sl = new AstList<FunctionVariable>();
      sl.add(var);
      AnyValue initial = new AnyValue();
      initial.metadata().add(info);
      VarDefInitStmt item = new VarDefInitStmt(sl, initial);
      item.metadata().add(info);
      ret.add(item);

      // assign initial value
      if (!(obj.initial instanceof AnyValue)) {
        if (firstVar == null) {
          AstList<LinkedReferenceWithOffset_Implementation> al = new AstList<LinkedReferenceWithOffset_Implementation>();
          al.add(RefFactory.oldFull(info, var));
          MultiAssignment ass = new MultiAssignment(al, obj.initial);
          ass.metadata().add(var.metadata());
          ret.add(ass);
          firstVar = var;
        } else {
          AstList<LinkedReferenceWithOffset_Implementation> al = new AstList<LinkedReferenceWithOffset_Implementation>();
          al.add(RefFactory.oldFull(info, var));
          ReferenceExpression right = new ReferenceExpression(RefFactory.oldFull(info, firstVar));
          right.metadata().add(info);
          MultiAssignment ass = new MultiAssignment(al, right);
          ass.metadata().add(var.metadata());
          ret.add(ass);
        }
      }
    }

    return ret;
  }

}
