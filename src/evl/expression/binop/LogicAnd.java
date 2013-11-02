package evl.expression.binop;

import common.ElementInfo;

import evl.expression.Expression;

final public class LogicAnd extends Logical {

  public LogicAnd(ElementInfo info, Expression left, Expression right) {
    super(info, left, right);
  }

  @Override
  public String getOpName() {
    return "and";
  }
}
