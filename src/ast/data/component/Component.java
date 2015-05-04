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

import ast.ElementInfo;
import ast.data.AstList;
import ast.data.Named;
import ast.data.component.composition.Direction;
import ast.data.component.composition.Queue;
import ast.data.function.Function;
import ast.data.function.InterfaceFunction;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSignal;
import ast.data.function.header.FuncSlot;
import ast.repository.TypeFilter;

abstract public class Component extends Named {
  public Queue queue;
  final public AstList<InterfaceFunction> iface = new AstList<InterfaceFunction>();
  final public AstList<Function> function = new AstList<Function>();

  public Component(ElementInfo info, String name) {
    super(info, name);
    queue = new Queue();
  }

  public AstList<InterfaceFunction> getIface(Direction dir) {
    AstList<InterfaceFunction> ret = new AstList<InterfaceFunction>();
    switch (dir) {
      case in: {
        ret.addAll(TypeFilter.select(iface, FuncResponse.class));
        ret.addAll(TypeFilter.select(iface, FuncSlot.class));
        break;
      }
      case out: {
        ret.addAll(TypeFilter.select(iface, FuncQuery.class));
        ret.addAll(TypeFilter.select(iface, FuncSignal.class));
        break;
      }
      default:
        throw new RuntimeException("Not implemented: " + dir);
    }
    return ret;
  }

}
