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
import ast.data.function.Function;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.Reference;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.CallStmt;
import ast.data.statement.MultiAssignment;
import ast.dispatcher.NullDispatcher;
import ast.specification.visitor.IsStateVariable;
import ast.visitor.VisitExecutorImplementation;
import error.RError;

public class StateChangeStmt extends Specification {
  static private final StateChangeDispatcher dispatcher = new StateChangeDispatcher();

  @Override
  public boolean isSatisfiedBy(Ast candidate) {
    return dispatcher.traverse(candidate, null);
  }

}

class StateChangeDispatcher extends NullDispatcher<Boolean, Void> {
  static private final PureFunction pureFunc = new PureFunction();

  @Override
  protected Boolean visitDefault(Ast obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitAssignmentSingle(AssignmentSingle obj, Void param) {
    return isStateVar(obj.getLeft());
  }

  @Override
  protected Boolean visitAssignmentMulti(MultiAssignment obj, Void param) {
    return containsStateVar(obj.getLeft());
  }

  @Override
  protected Boolean visitCallStmt(CallStmt obj, Void param) {
    return isImpure(obj.call);
  }

  private boolean isImpure(Reference call) {
    Function target = (Function) ((LinkedAnchor) call.getAnchor()).getLink();
    return !pureFunc.isSatisfiedBy(target);
  }

  private boolean isStateVar(Ast var) {
    IsStateVariable isStateVariable = new IsStateVariable(VisitExecutorImplementation.instance(), RError.instance());

    VisitExecutorImplementation.instance().visit(isStateVariable, var);

    return isStateVariable.isState();
  }

  private boolean containsStateVar(AstList<Reference> vars) {
    for (Reference left : vars) {
      if (isStateVar(((LinkedAnchor) left.getAnchor()).getLink())) {
        return true;
      }
    }
    return false;
  }

}
