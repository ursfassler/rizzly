package evl.expression;

import common.ElementInfo;

public class UnaryExpression extends Expression {
  private Expression expr;
  private UnaryOp op;

  public UnaryExpression(ElementInfo info, Expression expr, UnaryOp op) {
    super(info);
    this.expr = expr;
    this.op = op;
  }

  public UnaryOp getOp() {
    return op;
  }

  public Expression getExpr() {
    return expr;
  }

  public void setExpr(Expression expr) {
    this.expr = expr;
  }

  public void setOp(UnaryOp op) {
    this.op = op;
  }

  @Override
  public String toString() {
    return "(" + op + " " + expr + ")";
  }
}
