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

package fun.expression;

import error.ErrorType;
import error.RError;

/**
 *
 * @author urs
 */
public enum RelOp {

  EQUAL, NOT_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUEAL, IS;

  @Override
  public String toString() {
    switch (this) {
      case EQUAL:
        return "=";
      case NOT_EQUAL:
        return "<>";
      case LESS:
        return "<";
      case LESS_EQUAL:
        return "<=";
      case GREATER:
        return ">";
      case GREATER_EQUEAL:
        return ">=";
      case IS:
        return "is";
      default:
        RError.err(ErrorType.Fatal, "not supported yet");
        return null;
    }
  }
}
