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

package evl.data.variable;

import common.ElementInfo;

import evl.data.expression.Expression;
import evl.data.expression.reference.SimpleRef;
import evl.data.type.Type;

final public class ConstGlobal extends Constant {
  public ConstGlobal(ElementInfo info, String name, SimpleRef<Type> type, Expression def) {
    super(info, name, type, def);
  }

}