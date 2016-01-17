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

package ast.data.component.composition;

import ast.data.Named;
import ast.data.component.CompRef;
import ast.meta.MetaList;
import ast.visitor.Visitor;

final public class CompUse extends Named {
  public CompRef compRef;

  public CompUse(String name, CompRef compRef) {
    setName(name);
    this.compRef = compRef;
  }

  @Deprecated
  public CompUse(MetaList info, String name, CompRef compRef) {
    metadata().add(info);
    setName(name);
    this.compRef = compRef;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

}
