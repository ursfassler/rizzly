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

import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.type.Type;

public class UnionValue extends Expression {
  private NamedElementValue tagValue;
  private NamedElementValue contentValue;
  private SimpleRef<Type> type;

  public UnionValue(ElementInfo info, NamedElementValue tagValue, NamedElementValue contentValue, SimpleRef<Type> type) {
    super(info);
    this.tagValue = tagValue;
    this.contentValue = contentValue;
    this.type = type;
  }

  public NamedElementValue getTagValue() {
    return tagValue;
  }

  public void setTagValue(NamedElementValue tagValue) {
    this.tagValue = tagValue;
  }

  public NamedElementValue getContentValue() {
    return contentValue;
  }

  public void setContentValue(NamedElementValue contentValue) {
    this.contentValue = contentValue;
  }

  public SimpleRef<Type> getType() {
    return type;
  }

  public void setType(SimpleRef<Type> type) {
    this.type = type;
  }
}
