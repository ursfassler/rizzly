package pir.statement;

import pir.expression.PExpression;
import pir.other.StateVariable;
import pir.other.Variable;

public class StoreStmt extends Statement {
  private Variable dst;
  private PExpression src;

  public StoreStmt(Variable dst, PExpression src) {
    this.dst = dst;
    this.src = src;
  }

  public PExpression getSrc() {
    return src;
  }

  public void setSrc(PExpression src) {
    this.src = src;
  }

  public Variable getDst() {
    return dst;
  }

  public void setDst(Variable dst) {
    this.dst = dst;
  }

  @Override
  public String toString() {
    return "store " + dst + " := " + src;
  }

}
