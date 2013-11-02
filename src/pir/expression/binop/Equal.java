package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
public class Equal extends Relation {

  public Equal(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "=";
  }
}
