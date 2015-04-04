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

package evl.composition;

import common.ElementInfo;

import evl.function.Function;
import evl.other.CompUse;

final public class EndpointSub extends Endpoint<CompUse> {
  final public String function;

  public EndpointSub(ElementInfo info, CompUse link, String function) {
    super(info, link);
    this.function = function;
  }

  @Override
  public Function getFunc() {
    return (Function) link.link.iface.find(function);
  }

  @Override
  public String toString() {
    return link.getName() + "." + function;
  }

}
