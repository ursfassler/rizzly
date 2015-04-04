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

package fun.composition;

import java.util.HashMap;
import java.util.Map;

import common.Direction;
import common.ElementInfo;

import fun.FunBase;
import fun.expression.reference.Reference;

public class Connection extends FunBase {
  final private Map<Direction, Reference> endpoint;
  private MessageType type;

  public Connection(ElementInfo info, Reference src, Reference dst, MessageType type) {
    super(info);
    this.type = type;
    endpoint = new HashMap<Direction, Reference>();
    endpoint.put(Direction.in, src);
    endpoint.put(Direction.out, dst);
  }

  public Reference getEndpoint(Direction dir) {
    return endpoint.get(dir);
  }

  public void setEndpoint(Direction dir, Reference ref) {
    endpoint.put(dir, ref);
  }

  public MessageType getType() {
    return type;
  }

  public void setType(MessageType type) {
    this.type = type;
  }

}
