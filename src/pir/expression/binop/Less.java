package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
public class Less extends Relation {

  public Less(Expression left, Expression right) {
    super(left, right);
  }

  @Override
  public String getOpName() {
    return "<";
  }
}
