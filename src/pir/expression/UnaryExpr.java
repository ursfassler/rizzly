package pir.expression;

public class UnaryExpr extends PExpression {
  final private UnOp op;
  final private PExpression expr;

  public UnaryExpr(UnOp op, PExpression expr) {
    super();
    this.op = op;
    this.expr = expr;
  }

  public UnOp getOp() {
    return op;
  }

  public PExpression getExpr() {
    return expr;
  }

  @Override
  public String toString() {
    return op.toString();
  }

}
