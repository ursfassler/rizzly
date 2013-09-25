package evl.expression.binop;

import common.ElementInfo;
import evl.expression.Expression;

/**
 * 
 * @author urs
 */
abstract public class ArithmeticOp extends BinaryExp {

  public ArithmeticOp(ElementInfo info, Expression left, Expression right) {
    super(info, left, right);
  }
}
