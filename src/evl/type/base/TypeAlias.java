package evl.type.base;

import common.ElementInfo;

import evl.expression.reference.Reference;
import evl.type.Type;

public class TypeAlias extends Type {
  private Reference ref;

  public TypeAlias(ElementInfo info, String name, Reference ref) {
    super(info, name);
    this.ref = ref;
  }

  public Reference getRef() {
    return ref;
  }

  public void setRef(Reference ref) {
    this.ref = ref;
  }
}
