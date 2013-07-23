package cir.expression;

public class BinaryOp extends Expression {
  private Expression left;
  private Expression right;
  private Op op;

  public BinaryOp(Expression left, Expression right, Op op) {
    super();
    this.left = left;
    this.right = right;
    this.op = op;
  }

  public Expression getLeft() {
    return left;
  }

  public Expression getRight() {
    return right;
  }

  public Op getOp() {
    return op;
  }

  public void setLeft(Expression left) {
    this.left = left;
  }

  public void setRight(Expression right) {
    this.right = right;
  }

  public void setOp(Op op) {
    this.op = op;
  }

  @Override
  public String toString() {
    return "(" + left + " " + op + " " + right + ")";
  }

}
