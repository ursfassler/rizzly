package evl.expression.binop;

import common.ElementInfo;

import evl.expression.Expression;

/**
 * 
 * @author urs
 */
public class BitAnd extends ArithmeticOp {

  public BitAnd(ElementInfo info, Expression left, Expression right) {
    super(info, left, right);
  }

  @Override
  public String getOpName() {
    return "and";
  }
}
