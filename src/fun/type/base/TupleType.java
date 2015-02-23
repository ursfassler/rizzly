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

package fun.type.base;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.other.FunList;

public class TupleType extends BaseType {
  final private FunList<Reference> types;

  public TupleType(ElementInfo info, String name, FunList<Reference> types) {
    super(info, name);
    this.types = types;
  }

  public TupleType(ElementInfo info, String name) {
    super(info, name);
    this.types = new FunList<Reference>();
  }

  public FunList<Reference> getTypes() {
    return types;
  }

}
