package fun.toevl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.expression.reference.Reference;
import evl.type.Type;
import evl.type.composed.NamedElement;
import evl.type.composed.UnionSelector;
import fun.Fun;
import fun.NullTraverser;
import fun.type.NamedType;
import fun.type.base.AnyType;
import fun.type.base.BooleanType;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.TypeAlias;
import fun.type.base.VoidType;
import fun.type.composed.RecordType;
import fun.type.composed.UnionType;
import fun.type.genfunc.Array;
import fun.type.genfunc.Range;
import fun.type.genfunc.TypeType;

public class FunToEvlType extends NullTraverser<Type, String> {
  private Map<Fun, Evl> map;
  private FunToEvl fta;

  public FunToEvlType(FunToEvl fta, Map<Fun, Evl> map) {
    super();
    this.map = map;
    this.fta = fta;
  }

  @Override
  protected Type visit(Fun obj, String param) {
    assert (param != null);
    evl.type.Type cobj = (Type) map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected Type visitDefault(Fun obj, String param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  // --------------------------------------------------------------------------

  @Override
  protected Type visitNamedType(NamedType obj, String param) {
    return visit(obj.getType(), param);
  }

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
    RError.err(ErrorType.Fatal, obj.getInfo(), "unresolved integer type: " + obj);
    return null;
  }

  @Override
  protected Type visitNaturalType(NaturalType obj, String param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "unresolved natural type: " + obj);
    return null;
  }

  @Override
  protected Type visitAnyType(AnyType obj, String param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "unresolved any type: " + obj);
    return null;
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
    return new evl.type.base.NumSet(new util.Range(obj.getLow(), obj.getHigh()));
  }

  @Override
  protected Type visitTypeAlias(TypeAlias obj, String param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "Alias tyoes should no longer occur");
    return null;
  }

  @Override
  protected Type visitEnumType(EnumType obj, String param) {
    List<evl.type.base.EnumElement> elements = new ArrayList<evl.type.base.EnumElement>();
    for (EnumElement elem : obj.getElement()) {
      elements.add((evl.type.base.EnumElement) fta.traverse(elem, null));
    }
    evl.type.base.EnumType ret = new evl.type.base.EnumType(obj.getInfo(), param,elements);
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
    UnionSelector selector = (UnionSelector) fta.traverse(obj.getSelector(), null);
    return new evl.type.composed.UnionType(obj.getInfo(), param, selector, element);
  }

  @Override
  protected Type visitArray(Array obj, String param) {
    Reference ref = (Reference) fta.traverse(obj.getType(), null);
    return new evl.type.base.ArrayType(obj.getSize(), FunToEvl.toTypeRef(ref));
  }

}
