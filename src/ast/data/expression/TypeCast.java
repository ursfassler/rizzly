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

package ast.data.expression;

import ast.ElementInfo;
import ast.data.type.TypeRef;
import ast.visitor.Visitor;

final public class TypeCast extends Expression {
  public Expression value;
  public TypeRef cast;

  public TypeCast(ElementInfo info, TypeRef cast, Expression value) {
    super(info);
    this.value = value;
    this.cast = cast;
  }

  @Override
  public String toString() {
    return cast + "(" + value + ")";
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
