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

package evl.data.expression.reference;

import common.ElementInfo;

import evl.data.EvlList;
import evl.data.Named;

final public class Reference extends BaseRef<Named> {
  public final EvlList<RefItem> offset;

  public Reference(ElementInfo info, Named link, EvlList<RefItem> offset) {
    super(info, link);
    assert (link != null);
    this.offset = new EvlList<RefItem>(offset);
  }

  public Reference(ElementInfo info, Named link, RefItem itm) {
    super(info, link);
    assert (link != null);
    this.offset = new EvlList<RefItem>();
    this.offset.add(itm);
  }

  public Reference(ElementInfo info, Named link) {
    super(info, link);
    assert (link != null);
    this.offset = new EvlList<RefItem>();
  }

  @Override
  public String toString() {
    String ret = super.toString();
    for (RefItem item : offset) {
      ret += item.toString();
    }
    return ret;
  }

}
