package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
public class Mul extends ArithmeticExp {

  public Mul(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "*";
  }
}
