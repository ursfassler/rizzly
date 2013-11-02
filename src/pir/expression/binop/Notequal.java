package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
public class Notequal extends Relation {

  public Notequal(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "<>";
  }
}
