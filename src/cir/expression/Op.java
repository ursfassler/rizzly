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

package cir.expression;

import error.ErrorType;
import error.RError;

public enum Op {
  PLUS, MINUS, MUL, DIV, BITOR, BITAND, LOCOR, LOCAND, MOD,

  EQUAL, NOT_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUEAL,

  LOCNOT, BITNOT,

  SHL, SHR;

  @Override
  public String toString() {
    switch (this) {
      case PLUS:
        return "+";
      case MINUS:
        return "-";
      case MUL:
        return "*";
      case DIV:
        return "/";
      case BITOR:
        return "|";
      case LOCOR:
        return "||";
      case BITAND:
        return "&";
      case LOCAND:
        return "&&";
      case MOD:
        return "%";
      case SHL:
        return "<<";
      case SHR:
        return ">>";
      case EQUAL:
        return "==";
      case NOT_EQUAL:
        return "!=";
      case LESS:
        return "<";
      case LESS_EQUAL:
        return "<=";
      case GREATER:
        return ">";
      case GREATER_EQUEAL:
        return ">=";
      case BITNOT:
        return "~";
      case LOCNOT:
        return "!";
      default:
        RError.err(ErrorType.Fatal, "not supported yet: " + this.ordinal());
        return null;
    }
  }

}
