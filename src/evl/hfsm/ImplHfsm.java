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

package evl.hfsm;

import common.ElementInfo;

import evl.other.Component;

public class ImplHfsm extends Component {
  private StateComposite topstate = null;

  public ImplHfsm(ElementInfo info, String name) {
    super(info, name);
  }

  public StateComposite getTopstate() {
    assert (topstate != null);
    return topstate;
  }

  public void setTopstate(StateComposite topstate) {
    assert (topstate != null);
    this.topstate = topstate;
  }

}
