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

package evl.type.base;

import java.math.BigInteger;

import common.ElementInfo;

import evl.Evl;
import evl.expression.reference.SimpleRef;
import evl.type.Type;

public class ArrayType extends BaseType implements Evl {
  private SimpleRef<Type> type;
  private BigInteger size;

  public ArrayType(BigInteger size, SimpleRef<Type> type) {
    super(ElementInfo.NO, makeName(size, type));
    this.type = type;
    this.size = size;
  }

  public ArrayType(ElementInfo info, String name, BigInteger size, SimpleRef<Type> type) {
    super(info, name);
    this.type = type;
    this.size = size;
  }

  public static String makeName(BigInteger size, SimpleRef<Type> type) {
    return "Array{" + size + "," + type.getLink().getName() + "}";
  }

  public String makeName() {
    return makeName(size, type);
  }

  public SimpleRef<Type> getType() {
    return type;
  }

  public void setType(SimpleRef<Type> type) {
    this.type = type;
  }

  public BigInteger getSize() {
    return size;
  }

}
