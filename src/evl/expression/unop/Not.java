package evl.expression.unop;

import common.ElementInfo;

import evl.expression.Expression;

/**
 * 
 * @author urs
 */
final public class Not extends UnaryExp {

  public Not(ElementInfo info, Expression expr) {
    super(info, expr);
  }

  @Override
  public String getOpName() {
    return "not";
  }

}
