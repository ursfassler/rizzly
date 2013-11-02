package pir.expression.unop;

import pir.expression.Expression;

/**
 *
 * @author urs
 */
final public class Not extends UnaryExp {

  public Not( Expression expr) {
    super( expr);
  }

  @Override
  public String getOpName() {
    return "not";
  }
  
}
