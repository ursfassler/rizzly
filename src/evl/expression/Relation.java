package evl.expression;

import common.ElementInfo;

/**
 * 
 * @author urs
 */
public class Relation extends Expression {

  private Expression left;
  private Expression right;
  private RelOp op;

  public Relation(ElementInfo info, Expression left, Expression right, RelOp op) {
    super(info);
    this.left = left;
    this.right = right;
    this.op = op;
  }

  public RelOp getOp() {
    return op;
  }

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
    return "(" + left + " " + op + " " + right + ")";
  }

}
