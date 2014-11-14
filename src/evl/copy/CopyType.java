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

package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.RecordType;
import evl.type.special.VoidType;

public class CopyType extends NullTraverser<Type, Void> {

  private CopyEvl cast;

  public CopyType(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Type visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitRangeType(RangeType obj, Void param) {
    return new RangeType(obj.getInfo(), obj.getName(), obj.getNumbers());
  }

  @Override
  protected Type visitRecordType(RecordType obj, Void param) {
    RecordType type = new RecordType(obj.getInfo(), obj.getName(), cast.copy(obj.getElement()));
    return type;
  }

  @Override
  protected Type visitStringType(StringType obj, Void param) {
    return new StringType();
  }

  @Override
  protected Type visitArrayType(ArrayType obj, Void param) {
    return new ArrayType(obj.getInfo(), obj.getName(), obj.getSize(), cast.copy(obj.getType()));
  }

  @Override
  protected Type visitEnumType(EnumType obj, Void param) {
    EnumType type = new EnumType(obj.getInfo(), obj.getName());
    type.getElement().addAll(cast.copy(obj.getElement()));
    return type;
  }

  @Override
  protected Type visitBooleanType(BooleanType obj, Void param) {
    return new BooleanType();
  }

  @Override
  protected Type visitVoidType(VoidType obj, Void param) {
    return new VoidType();
  }

}
