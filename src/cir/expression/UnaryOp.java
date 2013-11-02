package cir.expression;

public class UnaryOp extends Expression {
  private Op op;
  private Expression expr;

  public UnaryOp(Op op, Expression expr) {
    super();
    this.op = op;
    this.expr = expr;
  }

  public Op getOp() {
    return op;
  }

  public Expression getExpr() {
    return expr;
  }

  public void setOp(Op op) {
    this.op = op;
  }

  public void setExpr(Expression expr) {
    this.expr = expr;
  }

  @Override
  public String toString() {
    return "(" + op + " " + expr + ")";
  }

}
