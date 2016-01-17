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
import ast.data.expression.binop.And;
import ast.data.expression.binop.BitXor;
import ast.data.expression.binop.Division;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Modulo;
import ast.data.expression.binop.Multiplication;
import ast.data.expression.binop.Or;
import ast.data.expression.binop.Plus;
import ast.data.expression.binop.Shl;
import ast.data.expression.binop.Shr;
import error.ErrorType;
import error.RError;

class ArithmeticOpFactory {
  static Expression create(Expression left, Expression right, ExpOp op) {
    switch (op) {
      case PLUS:
        return new Plus(left, right);
      case MINUS:
        return new Minus(left, right);
      case MUL:
        return new Multiplication(left, right);
      case DIV:
        return new Division(left, right);
      case MOD:
        return new Modulo(left, right);
      case AND:
        return new And(left, right);
      case OR:
        return new Or(left, right);
      case SHL:
        return new Shl(left, right);
      case SHR:
        return new Shr(left, right);
      case XOR:
        return new BitXor(left, right);
      default:
        RError.err(ErrorType.Fatal, "Unhandled ArithmeticOp: " + op);
    }
    return null;
  }
}
