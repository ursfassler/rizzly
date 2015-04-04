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

import evl.data.Evl;
import evl.data.type.Type;
import evl.data.type.base.ArrayType;
import evl.data.type.base.BooleanType;
import evl.data.type.base.EnumType;
import evl.data.type.base.RangeType;
import evl.data.type.base.StringType;
import evl.data.type.composed.RecordType;
import evl.data.type.composed.UnionType;
import evl.data.type.composed.UnsafeUnionType;
import evl.data.type.out.AliasType;
import evl.data.type.special.VoidType;
import evl.traverser.NullTraverser;

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
    return new RangeType(obj.getInfo(), obj.getName(), obj.range);
  }

  @Override
  protected Type visitRecordType(RecordType obj, Void param) {
    RecordType type = new RecordType(obj.getInfo(), obj.getName(), cast.copy(obj.element));
    return type;
  }

  @Override
  protected Type visitStringType(StringType obj, Void param) {
    return new StringType();
  }

  @Override
  protected Type visitArrayType(ArrayType obj, Void param) {
    return new ArrayType(obj.getInfo(), obj.getName(), obj.size, cast.copy(obj.type));
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

  @Override
  protected Type visitUnionType(UnionType obj, Void param) {
    UnionType type = new UnionType(obj.getInfo(), obj.getName(), cast.copy(obj.element), cast.copy(obj.tag));
    return type;
  }

  @Override
  protected Type visitUnsafeUnionType(UnsafeUnionType obj, Void param) {
    UnsafeUnionType type = new UnsafeUnionType(obj.getInfo(), obj.getName(), cast.copy(obj.element));
    return type;
  }

  @Override
  protected Type visitAliasType(AliasType obj, Void param) {
    return new AliasType(obj.getInfo(), obj.getName(), cast.copy(obj.ref));
  }

}
