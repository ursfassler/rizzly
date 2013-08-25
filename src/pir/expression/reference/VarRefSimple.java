package pir.expression.reference;

import pir.expression.PExpression;
import pir.other.PirValue;
import pir.other.SsaVariable;
import pir.type.TypeRef;

final public class VarRefSimple extends PExpression implements Reference<SsaVariable>, PirValue {
  private SsaVariable ref;

  public VarRefSimple(SsaVariable ref) {
    super();
    this.ref = ref;
  }

  @Override
  public SsaVariable getRef() {
    return ref;
  }

  @Override
  public void setRef(SsaVariable ref) {
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
