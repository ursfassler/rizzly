package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
public class BitAnd extends ArithmeticExp {

  public BitAnd(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "and";
  }
}
