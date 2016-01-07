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

import ast.ElementInfo;
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
  static Relation create(ElementInfo info, Expression left, Expression right, RelOp op) {
    switch (op) {
      case EQUAL:
        return new Equal(info, left, right);
      case NOT_EQUAL:
        return new NotEqual(info, left, right);
      case GREATER:
        return new Greater(info, left, right);
      case GREATER_EQUEAL:
        return new GreaterEqual(info, left, right);
      case LESS:
        return new Less(info, left, right);
      case LESS_EQUAL:
        return new LessEqual(info, left, right);
      case IS:
        return new Is(info, left, right);
      default:
        RError.err(ErrorType.Fatal, info, "Unhandled relation: " + op);
    }
    return null;
  }
}
