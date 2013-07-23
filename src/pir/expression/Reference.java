package pir.expression;

import pir.expression.reference.RefItem;

final public class Reference extends PExpression {
  private RefItem ref;

  public Reference(RefItem ref) {
    super();
    this.ref = ref;
  }

  public RefItem getRef() {
    return ref;
  }

  public void setRef(RefItem ref) {
    this.ref = ref;
  }

  @Override
  public String toString() {
    return ref.toString();
  }

}
