package cir.type;

import cir.CirBase;

public class TypeRef extends CirBase {
  private Type ref;

  public TypeRef(Type ref) {
    super();
    this.ref = ref;
  }

  public Type getRef() {
    return ref;
  }

  public void setRef(Type ref) {
    this.ref = ref;
  }

}
