package pir.expression.reference;

import pir.expression.PExpression;

final public class RefIndex extends RefMiddle {
  private PExpression index;

  public RefIndex(RefItem previous, PExpression index) {
    super(previous);
    this.index = index;
  }

  public PExpression getIndex() {
    return index;
  }

  public void setIndex(PExpression index) {
    this.index = index;
  }

  @Override
  public String toString() {
    return "[" + index + "]";
  }

}
