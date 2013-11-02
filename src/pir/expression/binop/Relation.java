package pir.expression.binop;

import pir.expression.Expression;

/**
 * 
 * @author urs
 */
abstract public class Relation extends BinaryExp {

  public Relation(Expression left, Expression right) {
    super(left, right);
  }
}
