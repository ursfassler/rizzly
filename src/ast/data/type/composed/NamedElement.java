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

package ast.data.type.composed;

import ast.data.Named;
import ast.data.reference.Reference;
import ast.meta.MetaList;

final public class NamedElement extends Named {
  public Reference typeref;

  public NamedElement(String name, Reference typeref) {
    setName(name);
    this.typeref = typeref;
  }

  @Deprecated
  public NamedElement(MetaList info, String name, Reference typeref) {
    metadata().add(info);
    setName(name);
    this.typeref = typeref;
  }

  @Override
  public String toString() {
    return getName() + typeref;
  }

}
