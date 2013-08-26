package pir.expression.reference;

import pir.expression.PExpression;
import pir.other.PirValue;
import pir.other.StateVariable;
import pir.type.TypeRef;

final public class VarRefStatevar extends PExpression implements Reference<StateVariable>, PirValue {
  private StateVariable ref;

  public VarRefStatevar(StateVariable ref) {
    super();
    this.ref = ref;
  }

  @Override
  public StateVariable getRef() {
    return ref;
  }

  @Override
  public void setRef(StateVariable ref) {
    this.ref = ref;
  }

  @Override
  public String toString() {
    return ref.toString();
  }

  @Override
  public TypeRef getType() {
    return ref.getType();
  }

}
