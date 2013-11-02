package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
public class Greaterequal extends Relation {

  public Greaterequal(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return ">=";
  }
}
