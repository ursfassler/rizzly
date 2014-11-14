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

package evl.other;

import common.ElementInfo;

import evl.expression.reference.BaseRef;

public class CompUse extends BaseRef<Component> implements Named, Comparable<CompUse> {
  private String name;

  public CompUse(ElementInfo info, Component link, String name) {
    super(info, link);
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int compareTo(CompUse o) {
    return name.compareTo(o.name);
  }

  @Override
  public String toString() {
    return name;
  }

}
