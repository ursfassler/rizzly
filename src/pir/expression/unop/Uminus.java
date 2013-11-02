package pir.expression.unop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
final public class Uminus extends UnaryExp {

  public Uminus(Expression expr) {
    super(expr);
  }

  @Override
  public String getOpName() {
    return "-";
  }

}
