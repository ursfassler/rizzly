package evl.statement.normal;

import common.ElementInfo;

import evl.expression.Expression;
import evl.expression.reference.Reference;

/**
 * 
 * @author urs
 */
public class Assignment extends NormalStmt {

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
