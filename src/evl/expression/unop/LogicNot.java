package evl.expression.unop;

import common.ElementInfo;

import evl.expression.Expression;

final public class LogicNot extends UnaryExp {

  public LogicNot(ElementInfo info, Expression expr) {
    super(info, expr);
  }

  @Override
  public String getOpName() {
    return "not";
  }

}
