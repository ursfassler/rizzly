package evl.expression.unop;

import common.ElementInfo;

import evl.expression.Expression;

final public class BitNot extends UnaryExp {

  public BitNot(ElementInfo info, Expression expr) {
    super(info, expr);
  }

  @Override
  public String getOpName() {
    return "not";
  }

}
