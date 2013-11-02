package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
final public class Plus extends ArithmeticExp {

  public Plus(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "+";
  }
}
