package evl.expression.unop;

import common.ElementInfo;

import evl.expression.Expression;

abstract public class UnaryExp extends Expression {
  private Expression expr;

  public UnaryExp(ElementInfo info, Expression expr) {
    super(info);
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
