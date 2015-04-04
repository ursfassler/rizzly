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

package evl.data.expression;

import java.util.Collection;

import common.ElementInfo;

import evl.data.EvlList;
import evl.data.expression.reference.SimpleRef;
import evl.data.type.Type;

public class RecordValue extends Expression {
  final public EvlList<NamedValue> value = new EvlList<NamedValue>();
  public SimpleRef<Type> type;

  public RecordValue(ElementInfo info, Collection<NamedValue> value, SimpleRef<Type> type) {
    super(info);
    this.value.addAll(value);
    this.type = type;
  }

  @Override
  public String toString() {
    return value + ": " + type;
  }

}