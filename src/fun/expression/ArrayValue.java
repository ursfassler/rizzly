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

import common.ElementInfo;

import fun.other.FunList;

public class ArrayValue extends Expression {
  final private FunList<Expression> value;

  public ArrayValue(ElementInfo info, FunList<Expression> value) {
    super(info);
    this.value = value;
  }

  public FunList<Expression> getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
