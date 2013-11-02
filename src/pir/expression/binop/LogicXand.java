package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
final public class LogicXand extends Logical {

  public LogicXand(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "=";
  }
}
