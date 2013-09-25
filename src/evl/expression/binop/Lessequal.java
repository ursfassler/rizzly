package evl.expression.binop;

import common.ElementInfo;

import evl.expression.Expression;

/**
 *
 * @author urs
 */
public class Lessequal extends Relation {

  public Lessequal(ElementInfo info, Expression left, Expression right) {
    super(info, left, right);
  }

  @Override
  public String getOpName() {
    return "<=";
  }
}