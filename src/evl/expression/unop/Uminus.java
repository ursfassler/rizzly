package evl.expression.unop;

import common.ElementInfo;

import evl.expression.Expression;

/**
 * 
 * @author urs
 */
final public class Uminus extends UnaryExp {

  public Uminus(ElementInfo info, Expression expr) {
    super(info, expr);
  }

  @Override
  public String getOpName() {
    return "-";
  }

}
