package fun.toevl;

import java.util.ArrayList;
import java.util.Collection;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.expression.reference.Reference;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.EnumDefRef;
import evl.type.base.EnumElement;
import evl.type.composed.NamedElement;
import fun.Fun;
import fun.NullTraverser;
import fun.type.base.AnyType;
import fun.type.base.BooleanType;
import fun.type.base.EnumType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.TypeAlias;
import fun.type.base.VoidType;
import fun.type.composed.RecordType;
import fun.type.composed.UnionType;
import fun.type.template.Array;
import fun.type.template.Range;
import fun.type.template.TypeType;

public class FunToEvlType extends NullTraverser<Type, String> {
  private FunToEvl fta;

  public FunToEvlType(FunToEvl fta) {
    super();
    this.fta = fta;
  }

  @Override
  protected Type visitDefault(Fun obj, String param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  // --------------------------------------------------------------------------

  @Override
  protected Type visitBooleanType(BooleanType obj, String param) {
    return new evl.type.base.BooleanType();
  }

  @Override
  protected Type visitVoidType(VoidType obj, String param) {
    return new evl.type.special.VoidType();
  }

  @Override
  protected Type visitIntegerType(IntegerType obj, String param) {
    return new evl.type.special.IntegerType();
  }

  @Override
  protected Type visitNaturalType(NaturalType obj, String param) {
    return new evl.type.special.NaturalType();
  }

  @Override
  protected Type visitAnyType(AnyType obj, String param) {
    return new evl.type.special.AnyType();
  }

  @Override
  protected Type visitTypeType(TypeType obj, String param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "unresolved type type: " + obj);
    return null;
  }

  @Override
  protected Type visitStringType(StringType obj, String param) {
    return new evl.type.base.StringType();
  }

  @Override
  protected Type visitRange(Range obj, String param) {
    return new evl.type.base.RangeType(new util.Range(obj.getLow(), obj.getHigh()));
  }

  @Override
  protected Type visitTypeAlias(TypeAlias obj, String param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "Alias tyoes should no longer occur");
    return null;
  }

  @Override
  protected Type visitEnumType(EnumType obj, String param) {
    evl.type.base.EnumType ret = new evl.type.base.EnumType(obj.getInfo(), param);
    fta.map.put(obj, ret);
    for (fun.expression.reference.Reference elem : obj.getElement()) {
      Reference ref = (Reference) fta.traverse(elem, null);
      assert (ref.getLink() instanceof EnumElement);
      assert (ref.getOffset().isEmpty());
      ret.getElement().add(new EnumDefRef(ref.getInfo(), (EnumElement) ref.getLink()));
    }
    return ret;
  }

  @Override
  protected Type visitRecordType(RecordType obj, String param) {
    Collection<NamedElement> element = new ArrayList<NamedElement>(obj.getSize());
    for (fun.type.composed.NamedElement elem : obj.getElement()) {
      element.add((NamedElement) fta.traverse(elem, null));
    }
    return new evl.type.composed.RecordType(obj.getInfo(), param, element);
  }

  @Override
  protected Type visitUnionType(UnionType obj, String param) {
    Collection<NamedElement> element = new ArrayList<NamedElement>(obj.getSize());
    for (fun.type.composed.NamedElement elem : obj.getElement()) {
      element.add((NamedElement) fta.traverse(elem, null));
    }

    NamedElement tag = new NamedElement(new ElementInfo(), Designator.NAME_SEP + "tag", new TypeRef(new ElementInfo(), new evl.type.special.VoidType()));
    // FIXME get singleton

    return new evl.type.composed.UnionType(obj.getInfo(), param, element, tag);
  }

  @Override
  protected Type visitArray(Array obj, String param) {
    Reference ref = (Reference) fta.traverse(obj.getType(), null);
    return new evl.type.base.ArrayType(obj.getSize(), FunToEvl.toTypeRef(ref));
  }

}
