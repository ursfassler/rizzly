package cir.statement;

import cir.expression.Expression;

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

  @Override
  public String toString() {
    return "return " + value + ";";
  }

}
