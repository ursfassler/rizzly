package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
public class Shl extends ArithmeticExp {

  public Shl(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "shl";
  }
}
