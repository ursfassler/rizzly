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

import common.ElementInfo;

import evl.data.Named;
import evl.data.component.Component;
import evl.data.expression.reference.BaseRef;
import evl.data.expression.reference.SimpleRef;

public class CompUse extends Named implements Comparable<CompUse> {
  final public BaseRef<Component> instance;

  public CompUse(ElementInfo info, String name, BaseRef<Component> instance) {
    super(info, name);
    this.instance = instance;
  }

  @Deprecated
  public CompUse(ElementInfo info, Component link, String name) {
    super(info, name);
    instance = new SimpleRef(info, link);
  }

  @Override
  public int compareTo(CompUse o) {
    return name.compareTo(o.name);
  }

}
