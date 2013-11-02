package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
public class Minus extends ArithmeticExp {

  public Minus(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "-";
  }
}
