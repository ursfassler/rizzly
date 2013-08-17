package pir.statement;

import pir.expression.PExpression;
import pir.expression.reference.VarRef;

public class ComplexWriter extends Statement {
  private VarRef dst;
  private PExpression src;

  public ComplexWriter(VarRef dst, PExpression src) {
    this.dst = dst;
    this.src = src;
  }

  public PExpression getSrc() {
    return src;
  }

  public void setSrc(PExpression src) {
    this.src = src;
  }

  public VarRef getDst() {
    return dst;
  }

  public void setDst(VarRef dst) {
    this.dst = dst;
  }

  @Override
  public String toString() {
    return dst + " := " + src;
  }

}
