package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
public class BitOr extends ArithmeticExp {

  public BitOr(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "or";
  }
}
