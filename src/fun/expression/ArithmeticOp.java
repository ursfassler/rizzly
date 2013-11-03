package fun.expression;

import common.ElementInfo;

/**
 * 
 * @author urs
 */
public class ArithmeticOp extends Expression {

  private Expression left;
  private Expression right;
  private ExpOp op;

  public ArithmeticOp(ElementInfo info, Expression left, Expression right, ExpOp op) {
    super(info);
    this.left = left;
    this.right = right;
    this.op = op;
  }

  public Expression getLeft() {
    return left;
  }

  public ExpOp getOp() {
    return op;
  }

  public Expression getRight() {
    return right;
  }

  public void setLeft(Expression left) {
    this.left = left;
  }

  public void setRight(Expression right) {
    this.right = right;
  }

  @Override
  public String toString() {
    return "(" + left + " " + op + " " + right + ")";
  }

}
