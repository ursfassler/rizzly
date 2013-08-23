package evl.expression;

import common.ElementInfo;

import evl.expression.reference.Reference;
import evl.type.TypeRef;

public class TypeCast extends Expression {
  private Reference ref; // TODO replace with simple reference
  private TypeRef cast;

  public TypeCast(ElementInfo info, Reference ref, TypeRef cast) {
    super(info);
    this.ref = ref;
    this.cast = cast;
  }

  public Reference getRef() {
    return ref;
  }

  public void setRef(Reference ref) {
    this.ref = ref;
  }

  public TypeRef getCast() {
    return cast;
  }

  public void setCast(TypeRef cast) {
    this.cast = cast;
  }

  @Override
  public String toString() {
    return cast + "(" + ref + ")";
  }
}
