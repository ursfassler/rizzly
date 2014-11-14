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

package evl.expression;

import java.util.Collection;

import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.other.EvlList;
import evl.type.Type;

public class RecordValue extends Expression {
  final private EvlList<NamedElementValue> value = new EvlList<NamedElementValue>();
  private SimpleRef<Type> type;

  public RecordValue(ElementInfo info, Collection<NamedElementValue> value, SimpleRef<Type> type) {
    super(info);
    this.value.addAll(value);
    this.type = type;
  }

  public EvlList<NamedElementValue> getValue() {
    return value;
  }

  public SimpleRef<Type> getType() {
    return type;
  }

  public void setType(SimpleRef<Type> type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return value + ": " + type;
  }

}
