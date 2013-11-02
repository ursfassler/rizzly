package evl.expression.binop;

import common.ElementInfo;

import evl.expression.Expression;

/**
 * 
 * @author urs
 */
final public class LogicXand extends Logical {

  public LogicXand(ElementInfo info, Expression left, Expression right) {
    super(info, left, right);
  }

  @Override
  public String getOpName() {
    return "=";
  }
}
