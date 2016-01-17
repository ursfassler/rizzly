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

package ast.data.component.hfsm;

import ast.data.reference.RefFactory;
import ast.meta.MetaList;

public class StateRefFactory {
  public static StateRef create(State comp) {
    return new StateRef(RefFactory.create(comp));
  }

  @Deprecated
  public static StateRef create(MetaList info, State comp) {
    return new StateRef(info, RefFactory.create(info, comp));
  }
}
