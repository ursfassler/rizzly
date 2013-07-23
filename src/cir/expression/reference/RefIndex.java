package cir.expression.reference;

import cir.expression.Expression;

final public class RefIndex extends RefMiddle {
  private Expression index;

  public RefIndex(RefItem previous, Expression index) {
    super(previous);
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
    return getPrevious().toString() + "[" + index + "]";
  }

}
