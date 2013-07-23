package fun.statement;

import common.ElementInfo;

import fun.expression.Expression;
import fun.expression.reference.Reference;

/**
 *
 * @author urs
 */
public class Assignment extends Statement {

  private Reference left;
  private Expression right;

  public Assignment(ElementInfo info, Reference left, Expression right) {
    super(info);
    this.left = left;
    this.right = right;
  }

  public Reference getLeft() {
    return left;
  }

  public Expression getRight() {
    return right;
  }

  public void setRight(Expression right) {
    this.right = right;
  }

  public void setLeft(Reference left) {
    this.left = left;
  }

  @Override
  public String toString() {
    return left + " := " + right;
  }

}
