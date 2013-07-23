package pir.statement;

import pir.expression.PExpression;
import pir.expression.Reference;

public class Assignment extends Statement {
  private Reference dst;
  private PExpression src;

  public Assignment(Reference dst, PExpression src) {
    super();
    this.dst = dst;
    this.src = src;
  }

  public Reference getDst() {
    return dst;
  }

  public PExpression getSrc() {
    return src;
  }

  public void setDst(Reference dst) {
    this.dst = dst;
  }

  public void setSrc(PExpression src) {
    this.src = src;
  }

  @Override
  public String toString() {
    return ":=";
  }

}
