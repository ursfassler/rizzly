package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
abstract public class BinaryExp extends Expression {

  private Expression left;
  private Expression right;

  public BinaryExp(Expression left, Expression right) {
    this.left = left;
    this.right = right;
  }

  abstract public String getOpName();

  public Expression getLeft() {
    return left;
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
    return "(" + left + " " + getOpName() + " " + right + ")";
  }
}
