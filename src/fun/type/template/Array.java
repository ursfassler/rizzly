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

package fun.type.template;

import java.math.BigInteger;

import common.ElementInfo;

import fun.expression.reference.SimpleRef;
import fun.type.Type;
import fun.type.base.BaseType;

public class Array extends BaseType {
  private SimpleRef type;
  private BigInteger size;

  public Array(ElementInfo info, BigInteger size, SimpleRef type) {
    super(info, makeName(size, type));
    this.type = type;
    this.size = size;
  }

  public static String makeName(BigInteger size, SimpleRef type) {
    return ArrayTemplate.NAME + "{" + size + "," + ((Type) type.getLink()).getName() + "}";
  }

  public SimpleRef getType() {
    return type;
  }

  public void setType(SimpleRef type) {
    this.type = type;
  }

  public void setSize(BigInteger size) {
    this.size = size;
  }

  public BigInteger getSize() {
    return size;
  }
}
