package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
public class Mod extends ArithmeticExp {

  public Mod(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "mod";
  }
}
