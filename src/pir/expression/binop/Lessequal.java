package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
public class Lessequal extends Relation {

  public Lessequal(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "<=";
  }
}
