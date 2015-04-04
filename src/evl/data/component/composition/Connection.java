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

package evl.data.component.composition;

import java.util.HashMap;
import java.util.Map;

import common.Direction;
import common.ElementInfo;

import evl.data.EvlBase;

public class Connection extends EvlBase {
  final public Map<Direction, Endpoint> endpoint;
  final public MessageType type;  // TODO remove type, create 2 new subclasses

  public Connection(ElementInfo info, Endpoint src, Endpoint dst, MessageType type) {
    super(info);
    this.type = type;
    endpoint = new HashMap<Direction, Endpoint>();
    endpoint.put(Direction.in, src);
    endpoint.put(Direction.out, dst);
  }

  @Override
  public String toString() {
    return endpoint.get(Direction.in) + " " + type + " " + endpoint.get(Direction.out);
  }

}
