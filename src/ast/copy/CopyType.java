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

package ast.copy;

import ast.data.Ast;
import ast.data.type.Type;
import ast.data.type.base.ArrayType;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.type.base.StringType;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnionType;
import ast.data.type.composed.UnsafeUnionType;
import ast.data.type.out.AliasType;
import ast.data.type.special.VoidType;
import ast.traverser.NullTraverser;

public class CopyType extends NullTraverser<Type, Void> {

  private CopyAst cast;

  public CopyType(CopyAst cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Type visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitRangeType(RangeType obj, Void param) {
    return new RangeType(obj.getInfo(), obj.name, obj.range);
  }

  @Override
  protected Type visitRecordType(RecordType obj, Void param) {
    RecordType type = new RecordType(obj.getInfo(), obj.name, cast.copy(obj.element));
    return type;
  }

  @Override
  protected Type visitStringType(StringType obj, Void param) {
    return new StringType();
  }

  @Override
  protected Type visitArrayType(ArrayType obj, Void param) {
    return new ArrayType(obj.getInfo(), obj.name, obj.size, cast.copy(obj.type));
  }

  @Override
  protected Type visitEnumType(EnumType obj, Void param) {
    EnumType type = new EnumType(obj.getInfo(), obj.name);
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
    UnionType type = new UnionType(obj.getInfo(), obj.name, cast.copy(obj.element), cast.copy(obj.tag));
    return type;
  }

  @Override
  protected Type visitUnsafeUnionType(UnsafeUnionType obj, Void param) {
    UnsafeUnionType type = new UnsafeUnionType(obj.getInfo(), obj.name, cast.copy(obj.element));
    return type;
  }

  @Override
  protected Type visitAliasType(AliasType obj, Void param) {
    return new AliasType(obj.getInfo(), obj.name, cast.copy(obj.ref));
  }

}
