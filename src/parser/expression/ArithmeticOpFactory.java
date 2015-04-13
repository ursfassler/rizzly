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

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.data.expression.Expression;
import evl.data.expression.binop.And;
import evl.data.expression.binop.BitXor;
import evl.data.expression.binop.Div;
import evl.data.expression.binop.Minus;
import evl.data.expression.binop.Mod;
import evl.data.expression.binop.Mul;
import evl.data.expression.binop.Or;
import evl.data.expression.binop.Plus;
import evl.data.expression.binop.Shl;
import evl.data.expression.binop.Shr;

class ArithmeticOpFactory {
  static Expression create(ElementInfo info, Expression left, Expression right, ExpOp op) {
    switch (op) {
      case PLUS:
        return new Plus(info, left, right);
      case MINUS:
        return new Minus(info, left, right);
      case MUL:
        return new Mul(info, left, right);
      case DIV:
        return new Div(info, left, right);
      case MOD:
        return new Mod(info, left, right);
      case AND:
        return new And(info, left, right);
      case OR:
        return new Or(info, left, right);
      case SHL:
        return new Shl(info, left, right);
      case SHR:
        return new Shr(info, left, right);
      case XOR:
        return new BitXor(info, left, right);
      default:
        RError.err(ErrorType.Fatal, info, "Unhandled ArithmeticOp: " + op);
    }
    return null;
  }
}
