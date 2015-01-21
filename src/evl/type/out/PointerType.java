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

package evl.type.out;

import common.ElementInfo;

import evl.expression.reference.BaseRef;
import evl.type.Type;

public class PointerType extends Type {
  private BaseRef<Type> type;

  public PointerType(ElementInfo info, String name, BaseRef<Type> type) {
    super(info, name);
    this.type = type;
  }

  public BaseRef<Type> getType() {
    return type;
  }

  public void setType(BaseRef<Type> type) {
    this.type = type;
  }

}
