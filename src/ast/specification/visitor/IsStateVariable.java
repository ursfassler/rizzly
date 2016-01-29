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

package ast.specification.visitor;

import ast.data.reference.LinkedReference;
import ast.data.reference.UnlinkedReference;
import ast.data.variable.StateVariable;
import ast.visitor.VisitExecutor;
import ast.visitor.Visitor;
import error.ErrorType;
import error.RizzlyError;

public class IsStateVariable implements Visitor {
  private final VisitExecutor executor;
  private final RizzlyError error;

  private boolean isState = false;

  public IsStateVariable(VisitExecutor executor, RizzlyError error) {
    this.executor = executor;
    this.error = error;
  }

  public boolean isState() {
    return isState;
  }

  public void visit(StateVariable object) {
    isState = true;
  }

  public void visit(LinkedReference object) {
    executor.visit(this, object.getLink());
  }

  public void visit(UnlinkedReference object) {
    error.err(ErrorType.Fatal, "can not decide for unlinked reference", object.metadata());
  }

}
