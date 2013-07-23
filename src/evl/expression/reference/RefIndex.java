package evl.expression.reference;

import common.ElementInfo;

import evl.expression.Expression;

final public class RefIndex extends RefItem {
  private Expression index;

  public RefIndex(ElementInfo info, Expression index) {
    super(info);
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
