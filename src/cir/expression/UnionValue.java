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

package cir.expression;

public class UnionValue extends Expression {
  private ElementValue tagValue;
  private ElementValue contentValue;

  public UnionValue(ElementValue tagValue, ElementValue contentValue) {
    super();
    this.tagValue = tagValue;
    this.contentValue = contentValue;
  }

  public ElementValue getTagValue() {
    return tagValue;
  }

  public void setTagValue(ElementValue tagValue) {
    this.tagValue = tagValue;
  }

  public ElementValue getContentValue() {
    return contentValue;
  }

  public void setContentValue(ElementValue contentValue) {
    this.contentValue = contentValue;
  }

}
