package evl.expression.binop;

import common.ElementInfo;

import evl.expression.Expression;

/**
 *
 * @author urs
 */
public class Shl extends ArithmeticOp {

  public Shl(ElementInfo info, Expression left, Expression right) {
    super(info, left, right);
  }

  @Override
  public String getOpName() {
    return "shl";
  }
}
