package evl.expression.binop;

import common.ElementInfo;

import evl.expression.Expression;

/**
 * 
 * @author urs
 */
abstract public class Relation extends BinaryExp {

  public Relation(ElementInfo info, Expression left, Expression right) {
    super(info, left, right);
  }
}
