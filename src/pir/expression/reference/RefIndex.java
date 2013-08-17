package pir.expression.reference;

import pir.expression.PExpression;

final public class RefIndex extends RefItem {
  private PExpression index;

  public RefIndex(PExpression index) {
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
