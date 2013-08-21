package evl.expression;

import common.ElementInfo;

import evl.type.TypeRef;
import evl.variable.SsaVariable;

public class TypeCast extends Expression {
  private SsaVariable ref;
  private TypeRef cast;

  public TypeCast(ElementInfo info, SsaVariable ref, TypeRef cast) {
    super(info);
    this.ref = ref;
    this.cast = cast;
  }

  public SsaVariable getRef() {
    return ref;
  }

  public void setRef(SsaVariable ref) {
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
