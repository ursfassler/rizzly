package pir.type;

import pir.PirObject;

final public class TypeRef extends PirObject {
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

  @Override
  public String toString() {
    return ref.toString();
  }

}
