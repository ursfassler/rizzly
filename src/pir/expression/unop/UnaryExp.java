package pir.expression.unop;

import pir.expression.Expression;

abstract public class UnaryExp extends Expression {
  private Expression expr;

  public UnaryExp(Expression expr) {
    this.expr = expr;
  }

  abstract public String getOpName();

  public Expression getExpr() {
    return expr;
  }

  public void setExpr(Expression expr) {
    this.expr = expr;
  }

  @Override
  public String toString() {
    return getOpName() + "(" + expr + ")";
  }
}
