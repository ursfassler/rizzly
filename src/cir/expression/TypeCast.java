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

import cir.type.TypeRef;

final public class TypeCast extends Expression {
  private Expression value;
  private TypeRef cast;

  public TypeCast(TypeRef cast, Expression value) {
    this.value = value;
    this.cast = cast;
  }

  public Expression getValue() {
    return value;
  }

  public void setValue(Expression value) {
    this.value = value;
  }

  public TypeRef getCast() {
    return cast;
  }

  public void setCast(TypeRef cast) {
    this.cast = cast;
  }

  @Override
  public String toString() {
    return cast + "(" + value + ")";
  }
}
