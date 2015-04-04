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

package fun.toevl;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.data.EvlList;
import evl.data.expression.reference.SimpleRef;
import evl.data.type.Type;
import evl.data.type.base.EnumElement;
import evl.data.type.composed.NamedElement;
import fun.Fun;
import fun.NullTraverser;
import fun.type.base.AnyType;
import fun.type.base.BooleanType;
import fun.type.base.EnumType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.VoidType;
import fun.type.composed.RecordType;
import fun.type.composed.UnionType;
import fun.type.template.Array;
import fun.type.template.Range;
import fun.type.template.TypeType;

public class FunToEvlType extends NullTraverser<Type, Void> {
  private FunToEvl fta;

  public FunToEvlType(FunToEvl fta) {
    super();
    this.fta = fta;
  }

  @Override
  protected Type visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  // --------------------------------------------------------------------------

  @Override
  protected Type visitBooleanType(BooleanType obj, Void param) {
    return new evl.data.type.base.BooleanType();
  }

  @Override
  protected Type visitVoidType(VoidType obj, Void param) {
    return new evl.data.type.special.VoidType();
  }

  @Override
  protected Type visitIntegerType(IntegerType obj, Void param) {
    return new evl.data.type.special.IntegerType();
  }

  @Override
  protected Type visitNaturalType(NaturalType obj, Void param) {
    return new evl.data.type.special.NaturalType();
  }

  @Override
  protected Type visitAnyType(AnyType obj, Void param) {
    return new evl.data.type.special.AnyType();
  }

  @Override
  protected Type visitTypeType(TypeType obj, Void param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "unresolved type type: " + obj);
    return null;
  }

  @Override
  protected Type visitStringType(StringType obj, Void param) {
    return new evl.data.type.base.StringType();
  }

  @Override
  protected Type visitRange(Range obj, Void param) {
    return new evl.data.type.base.RangeType(obj.getInfo(), obj.getName(), new util.Range(obj.getLow(), obj.getHigh()));
  }

  @Override
  protected Type visitEnumType(EnumType obj, Void param) {
    evl.data.type.base.EnumType ret = new evl.data.type.base.EnumType(obj.getInfo(), obj.getName());
    for (fun.type.base.EnumElement elem : obj.getElement()) {
      ret.getElement().add((EnumElement) fta.visit(elem, null));
    }
    return ret;
  }

  @Override
  protected Type visitRecordType(RecordType obj, Void param) {
    EvlList<NamedElement> element = new EvlList<NamedElement>();
    for (fun.type.composed.NamedElement elem : obj.getElement()) {
      element.add((NamedElement) fta.traverse(elem, null));
    }
    return new evl.data.type.composed.RecordType(obj.getInfo(), obj.getName(), element);
  }

  @Override
  protected Type visitUnionType(UnionType obj, Void param) {
    EvlList<NamedElement> element = new EvlList<NamedElement>();
    for (fun.type.composed.NamedElement elem : obj.getElement()) {
      element.add((NamedElement) fta.traverse(elem, null));
    }

    Type voidType = (Type) fta.traverse(VoidType.INSTANCE, param);
    NamedElement tag = new NamedElement(ElementInfo.NO, Designator.NAME_SEP + "tag", new SimpleRef<Type>(ElementInfo.NO, voidType));
    // FIXME get singleton

    return new evl.data.type.composed.UnionType(obj.getInfo(), obj.getName(), element, tag);
  }

  @Override
  protected Type visitArray(Array obj, Void param) {
    SimpleRef<Type> ref = (SimpleRef<Type>) fta.traverse(obj.getType(), null);
    return new evl.data.type.base.ArrayType(obj.getInfo(), obj.getName(), obj.getSize(), ref);
  }

}
