package fun.type.base;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.type.TypeGenerator;

public class TypeAlias extends TypeGenerator {
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
