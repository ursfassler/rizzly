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

package ast.data.type.out;

import ast.data.type.TypeReference;
import ast.data.type.base.BaseType;
import ast.meta.MetaList;
import ast.visitor.Visitor;

final public class AliasType extends BaseType {
  public TypeReference ref;

  public AliasType(String name, TypeReference ref) {
    super(name);
    this.ref = ref;
  }

  @Deprecated
  public AliasType(MetaList info, String name, TypeReference ref) {
    super(info, name);
    this.ref = ref;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
