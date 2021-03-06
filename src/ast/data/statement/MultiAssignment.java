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

package ast.data.statement;

import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.reference.Reference;

public class MultiAssignment extends Assignment {
  private final AstList<Reference> left;

  public MultiAssignment(AstList<Reference> left, Expression right) {
    super(right);
    this.left = left;
  }

  @Override
  public String toString() {
    return getLeft() + " := " + getRight();
  }

  public AstList<Reference> getLeft() {
    return left;
  }

}
