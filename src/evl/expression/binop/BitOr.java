package evl.expression.binop;

import common.ElementInfo;

import evl.expression.Expression;

/**
 * 
 * @author urs
 */
public class BitOr extends ArithmeticOp {

  public BitOr(ElementInfo info, Expression left, Expression right) {
    super(info, left, right);
  }

  @Override
  public String getOpName() {
    return "or";
  }
}
