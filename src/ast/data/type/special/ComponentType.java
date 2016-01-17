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

package ast.data.type.special;

import ast.data.AstList;
import ast.data.type.Type;
import ast.data.type.composed.NamedElement;
import ast.meta.MetaList;
import ast.visitor.Visitor;

final public class ComponentType extends Type {
  final public AstList<NamedElement> input = new AstList<NamedElement>();
  final public AstList<NamedElement> output = new AstList<NamedElement>();

  public ComponentType(String name) {
    super(name);
  }

  @Deprecated
  public ComponentType(MetaList info, String name) {
    super(info, name);
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
