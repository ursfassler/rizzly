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

package cir.type;

public class ArrayType extends Type {
  private TypeRef type;
  private int size;

  public ArrayType(String name, TypeRef type, int size) {
    super(name);
    this.type = type;
    this.size = size;
  }

  public TypeRef getType() {
    return type;
  }

  public int getSize() {
    return size;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }

  public void setSize(int size) {
    this.size = size;
  }

}
