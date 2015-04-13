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

package ast.pass.modelcheck.io;

import ast.data.Ast;
import ast.data.expression.reference.Reference;
import ast.data.statement.AssignmentMulti;
import ast.data.statement.AssignmentSingle;
import ast.data.variable.StateVariable;
import ast.data.variable.Variable;
import ast.traverser.DefTraverser;

/**
 * Returns for every function if it writes state. It does not check called functions nor it uses information from the
 * function type or header.
 *
 * @author urs
 *
 */
public class StateWriterInfo extends DefTraverser<Void, Void> {
  private boolean writeState = false;

  public static boolean get(Ast inst) {
    StateWriterInfo reduction = new StateWriterInfo();
    reduction.traverse(inst, null);
    return reduction.writeState;
  }

  private boolean isStateVariable(Variable var) {
    return var instanceof StateVariable;
  }

  private void checkRef(Reference ref) {
    if (ref.link instanceof Variable) {
      Variable var = (Variable) ref.link;
      if (isStateVariable(var)) {
        writeState = true;
      }
    }
  }

  @Override
  protected Void visitAssignmentSingle(AssignmentSingle obj, Void param) {
    Reference ref = obj.left;
    checkRef(ref);
    return null;
  }

  @Override
  protected Void visitAssignmentMulti(AssignmentMulti obj, Void param) {
    for (Reference ref : obj.left) {
      checkRef(ref);
    }
    return null;
  }
}
