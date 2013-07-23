package pir.statement;

import pir.expression.PExpression;

final public class CaseOptValue extends CaseOptEntry {
  private PExpression value;

  public CaseOptValue(PExpression value) {
    super();
    this.value = value;
  }

  public PExpression getValue() {
    return value;
  }

  public void setValue(PExpression value) {
    this.value = value;
  }

}
