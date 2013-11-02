package pir.expression.binop;

import pir.expression.Expression;

final public class LogicAnd extends Logical{

  public LogicAnd( Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "and";
  }
}
