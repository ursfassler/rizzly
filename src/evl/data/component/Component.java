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

package evl.data.component;

import common.Direction;
import common.ElementInfo;

import evl.data.EvlList;
import evl.data.Named;
import evl.data.component.composition.Queue;
import evl.data.function.Function;
import evl.data.function.InterfaceFunction;
import evl.data.function.header.FuncQuery;
import evl.data.function.header.FuncResponse;
import evl.data.function.header.FuncSignal;
import evl.data.function.header.FuncSlot;
import evl.traverser.other.ClassGetter;

abstract public class Component extends Named {
  public Queue queue;
  final public EvlList<InterfaceFunction> iface = new EvlList<InterfaceFunction>();
  final public EvlList<Function> function = new EvlList<Function>();

  public Component(ElementInfo info, String name) {
    super(info, name);
    queue = new Queue();
  }

  public EvlList<InterfaceFunction> getIface(Direction dir) {
    EvlList<InterfaceFunction> ret = new EvlList<InterfaceFunction>();
    switch (dir) {
      case in: {
        ret.addAll(ClassGetter.filter(FuncResponse.class, iface));
        ret.addAll(ClassGetter.filter(FuncSlot.class, iface));
        break;
      }
      case out: {
        ret.addAll(ClassGetter.filter(FuncQuery.class, iface));
        ret.addAll(ClassGetter.filter(FuncSignal.class, iface));
        break;
      }
      default:
        throw new RuntimeException("Not implemented: " + dir);
    }
    return ret;
  }

}
