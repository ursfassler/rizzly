package pir.statement;

import pir.expression.PExpression;

public class ReturnValue extends Return {
  private PExpression value;

  public ReturnValue(PExpression value) {
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
