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

package ast.specification;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.expression.reference.Reference;
import ast.data.function.Function;
import ast.data.statement.AssignmentMulti;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.CallStmt;
import ast.data.variable.StateVariable;
import ast.traverser.NullTraverser;

public class StateChangeStmt extends Specification {
  static private final StateChangeDispatcher dispatcher = new StateChangeDispatcher();

  @Override
  public boolean isSatisfiedBy(Ast candidate) {
    return dispatcher.traverse(candidate, null);
  }

}

class StateChangeDispatcher extends NullTraverser<Boolean, Void> {
  static private final PureFunction pureFunc = new PureFunction();

  @Override
  protected Boolean visitDefault(Ast obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitAssignmentSingle(AssignmentSingle obj, Void param) {
    return isStateVar(obj.left.link);
  }

  @Override
  protected Boolean visitAssignmentMulti(AssignmentMulti obj, Void param) {
    return containsStateVar(obj.left);
  }

  @Override
  protected Boolean visitCallStmt(CallStmt obj, Void param) {
    return isImpure(obj.call);
  }

  private boolean isImpure(Reference call) {
    Function target = (Function) call.link;
    return !pureFunc.isSatisfiedBy(target);
  }

  private boolean isStateVar(Ast var) {
    return var instanceof StateVariable;
  }

  private boolean containsStateVar(AstList<Reference> vars) {
    for (Reference left : vars) {
      if (isStateVar(left.link)) {
        return true;
      }
    }
    return false;
  }

}
