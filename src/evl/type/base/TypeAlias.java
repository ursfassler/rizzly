package evl.type.base;

import common.ElementInfo;

import evl.type.Type;
import evl.type.TypeRef;

public class TypeAlias extends Type {
  private TypeRef ref;

  public TypeAlias(ElementInfo info, String name, TypeRef ref) {
    super(info, name);
    this.ref = ref;
  }

  public TypeRef getRef() {
    return ref;
  }

  public void setRef(TypeRef ref) {
    this.ref = ref;
  }
}
