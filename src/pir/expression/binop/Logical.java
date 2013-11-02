package pir.expression.binop;

import pir.expression.Expression;

public abstract class Logical extends BinaryExp {

  public Logical(Expression left, Expression right) {
    super(left, right);
  }

}
