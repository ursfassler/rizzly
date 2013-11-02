package pir.statement;

import pir.expression.Expression;

public class ReturnExpr extends Return {
  private Expression value;

  public ReturnExpr(Expression value) {
    super();
    this.value = value;
  }

  public Expression getValue() {
    return value;
  }

  public void setValue(Expression value) {
    this.value = value;
  }

}
