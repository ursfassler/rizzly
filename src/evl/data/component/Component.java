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

import evl.data.EvlBase;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.component.composition.Queue;
import evl.data.function.Function;
import evl.data.function.InterfaceFunction;
import evl.data.function.header.FuncCtrlInDataIn;
import evl.data.function.header.FuncCtrlInDataOut;
import evl.data.function.header.FuncCtrlOutDataIn;
import evl.data.function.header.FuncCtrlOutDataOut;

abstract public class Component extends EvlBase implements Named {
  private String name;
  public Queue queue;
  final public EvlList<InterfaceFunction> iface = new EvlList<InterfaceFunction>();
  final public EvlList<Function> function = new EvlList<Function>();

  public Component(ElementInfo info, String name) {
    super(info);
    this.name = name;
    queue = new Queue();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public EvlList<InterfaceFunction> getIface(Direction dir) {
    EvlList<InterfaceFunction> ret = new EvlList<InterfaceFunction>();
    switch (dir) {
      case in: {
        ret.addAll(iface.getItems(FuncCtrlInDataOut.class));
        ret.addAll(iface.getItems(FuncCtrlInDataIn.class));
        break;
      }
      case out: {
        ret.addAll(iface.getItems(FuncCtrlOutDataIn.class));
        ret.addAll(iface.getItems(FuncCtrlOutDataOut.class));
        break;
      }
      default:
        throw new RuntimeException("Not implemented: " + dir);
    }
    return ret;
  }

  @Override
  public String toString() {
    return name;
  }

}
