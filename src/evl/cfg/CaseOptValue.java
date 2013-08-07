package evl.cfg;

import common.ElementInfo;

import evl.expression.Expression;

public class CaseOptValue extends CaseOptEntry {
  private Expression value;

  public CaseOptValue(ElementInfo info, Expression value) {
    super(info);
    this.value = value;
  }

  public Expression getValue() {
    return value;
  }

  public void setValue(Expression value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
