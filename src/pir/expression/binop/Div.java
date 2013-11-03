package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
public class Div extends ArithmeticExp {

  public Div(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "/";
  }
}
