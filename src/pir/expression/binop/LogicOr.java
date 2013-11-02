package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
final public class LogicOr extends Logical {

  public LogicOr(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "or";
  }
}
