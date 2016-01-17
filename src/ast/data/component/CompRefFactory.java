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

import ast.data.raw.RawComponent;
import ast.data.reference.RefFactory;
import ast.meta.MetaList;

public class CompRefFactory {
  public static CompRef create(Component comp) {
    return new CompRef(RefFactory.create(comp));
  }

  @Deprecated
  public static CompRef create(MetaList info, Component comp) {
    return new CompRef(info, RefFactory.create(info, comp));
  }

  public static CompRef create(RawComponent comp) {
    return new CompRef(RefFactory.create(comp));
  }

}
