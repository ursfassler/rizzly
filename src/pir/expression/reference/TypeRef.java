package pir.expression.reference;

import pir.expression.PExpression;
import pir.type.Type;

final public class TypeRef extends PExpression implements Reference<Type> {
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
