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

import ast.data.AstList;
import ast.data.type.Type;
import ast.meta.MetaList;

abstract public class NamedElementType extends Type {
  final public AstList<NamedElement> element;

  public NamedElementType(String name, AstList<NamedElement> element) {
    super(name);
    this.element = element;
  }

  @Deprecated
  public NamedElementType(MetaList info, String name, AstList<NamedElement> element) {
    super(info, name);
    this.element = element;
  }

  public NamedElementType(String name) {
    super(name);
    this.element = new AstList<NamedElement>();
  }

  @Deprecated
  public NamedElementType(MetaList info, String name) {
    super(info, name);
    this.element = new AstList<NamedElement>();
  }

}
