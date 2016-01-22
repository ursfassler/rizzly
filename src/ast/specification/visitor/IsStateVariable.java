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

import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.reference.UnlinkedReferenceWithOffset_Implementation;
import ast.data.variable.StateVariable;
import ast.visitor.NullVisitor;
import error.ErrorType;
import error.RizzlyError;

public class IsStateVariable extends NullVisitor {
  private final RizzlyError error;

  private boolean isState = false;

  public IsStateVariable(RizzlyError error) {
    super();
    this.error = error;
  }

  public boolean isState() {
    return isState;
  }

  @Override
  public void visit(StateVariable object) {
    isState = true;
  }

  @Override
  public void visit(LinkedReferenceWithOffset_Implementation object) {
    object.getLink().accept(this);
  }

  @Override
  public void visit(UnlinkedReferenceWithOffset_Implementation object) {
    error.err(ErrorType.Fatal, "can not decide for unlinked reference", object.metadata());
  }

}
