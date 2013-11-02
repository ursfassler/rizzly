package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
abstract public class ArithmeticExp extends BinaryExp {

  public ArithmeticExp(Expression left, Expression right) {
    super(left, right);
  }
}
