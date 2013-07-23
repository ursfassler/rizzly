package cir.statement;

import cir.expression.Expression;
import cir.expression.Reference;

public class Assignment extends Statement {
  private Reference dst;
  private Expression src;

  public Assignment(Reference dst, Expression src) {
    super();
    this.dst = dst;
    this.src = src;
  }

  public Reference getDst() {
    return dst;
  }

  public Expression getSrc() {
    return src;
  }

  public void setDst(Reference dst) {
    this.dst = dst;
  }

  public void setSrc(Expression src) {
    this.src = src;
  }

  @Override
  public String toString() {
    return dst + " = " + src;
  }

}
