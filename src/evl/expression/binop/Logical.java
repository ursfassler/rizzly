package evl.expression.binop;

import common.ElementInfo;

import evl.expression.Expression;

public abstract class Logical extends BinaryExp {

  public Logical(ElementInfo info, Expression left, Expression right) {
    super(info, left, right);
  }

}
