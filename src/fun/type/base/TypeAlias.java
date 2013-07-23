package fun.type.base;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.type.Type;

public class TypeAlias extends Type {
  private Reference ref;

  public TypeAlias(ElementInfo info, Reference ref) {
    super(info);
    this.ref = ref;
  }

  public Reference getRef() {
    return ref;
  }

  public void setRef(Reference ref) {
    this.ref = ref;
  }
}
