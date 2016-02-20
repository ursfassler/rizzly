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

package ast.data.expression.value;

import java.util.Collection;

import ast.data.AstList;
import ast.data.reference.Reference;

final public class RecordValue extends ValueExpr {
  final public AstList<NamedValue> value = new AstList<NamedValue>();
  public Reference type;

  public RecordValue(Collection<NamedValue> value, Reference type) {
    this.value.addAll(value);
    this.type = type;
  }

  @Override
  public String toString() {
    return value + ":" + type;
  }

}
