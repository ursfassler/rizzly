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

package parser.expression;

import ast.data.expression.Expression;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.GreaterEqual;
import ast.data.expression.binop.Is;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.LessEqual;
import ast.data.expression.binop.NotEqual;
import ast.data.expression.binop.Relation;
import error.ErrorType;
import error.RError;

class RelationFactory {
  static Relation create(Expression left, Expression right, RelOp op) {
    switch (op) {
      case EQUAL:
        return new Equal(left, right);
      case NOT_EQUAL:
        return new NotEqual(left, right);
      case GREATER:
        return new Greater(left, right);
      case GREATER_EQUEAL:
        return new GreaterEqual(left, right);
      case LESS:
        return new Less(left, right);
      case LESS_EQUAL:
        return new LessEqual(left, right);
      case IS:
        return new Is(left, right);
      default:
        RError.err(ErrorType.Fatal, "Unhandled relation: " + op);
    }
    return null;
  }
}
