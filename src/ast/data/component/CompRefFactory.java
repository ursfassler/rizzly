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

package ast.data.component;

import ast.data.AstList;
import ast.data.raw.RawComponent;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefFactory;
import ast.data.reference.RefItem;
import ast.meta.MetaList;

//TODO inline calls, remove class
@Deprecated
public class CompRefFactory {
  public static OffsetReference create(Component comp) {
    return RefFactory.create(comp, new AstList<RefItem>());
  }

  @Deprecated
  public static OffsetReference create(MetaList info, Component comp) {
    return RefFactory.create(info, comp, new AstList<RefItem>());
  }

  public static OffsetReference create(RawComponent comp) {
    return RefFactory.create(comp, new AstList<RefItem>());
  }
}
