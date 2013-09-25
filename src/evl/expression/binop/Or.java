package evl.expression.binop;

import common.ElementInfo;

import evl.expression.Expression;

/**
 *
 * @author urs
 */
public class Or extends ArithmeticOp {

  public Or(ElementInfo info, Expression left, Expression right) {
    super(info, left, right);
  }

  @Override
  public String getOpName() {
    return "or";
  }
}
