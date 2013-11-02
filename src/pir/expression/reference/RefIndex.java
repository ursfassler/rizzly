package pir.expression.reference;

import pir.expression.Expression;

final public class RefIndex extends RefItem {
  private Expression index;

  public RefIndex(Expression index) {
    this.index = index;
  }

  public Expression getIndex() {
    return index;
  }

  public void setIndex(Expression index) {
    this.index = index;
  }

  @Override
  public String toString() {
    return "[" + index + "]";
  }

}
