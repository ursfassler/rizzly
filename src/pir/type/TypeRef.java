package pir.type;

import pir.PirObject;
import pir.expression.reference.Reference;

final public class TypeRef extends PirObject implements Reference<Type> {
  private Type ref;

  public TypeRef(Type ref) {
    super();
    this.ref = ref;
  }

  @Override
  public Type getRef() {
    return ref;
  }

  @Override
  public void setRef(Type ref) {
    this.ref = ref;
  }

  @Override
  public String toString() {
    return ref.toString();
  }

}
