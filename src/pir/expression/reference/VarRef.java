package pir.expression.reference;

import pir.expression.PExpression;
import pir.other.Variable;

final public class VarRef extends PExpression implements Reference<Variable> {
  private Variable ref;

  public VarRef(Variable ref) {
    super();
    this.ref = ref;
  }

  @Override
  public Variable getRef() {
    return ref;
  }

  @Override
  public void setRef(Variable ref) {
    this.ref = ref;
  }

  @Override
  public String toString() {
    return ref.toString();
  }

}
