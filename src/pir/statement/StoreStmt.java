package pir.statement;

import pir.expression.PExpression;
import pir.other.StateVariable;

public class StoreStmt extends Statement {
  private StateVariable dst;
  private PExpression src;

  public StoreStmt(StateVariable dst, PExpression src) {
    this.dst = dst;
    this.src = src;
  }

  public PExpression getSrc() {
    return src;
  }

  public void setSrc(PExpression src) {
    this.src = src;
  }

  public StateVariable getDst() {
    return dst;
  }

  public void setDst(StateVariable dst) {
    this.dst = dst;
  }

  @Override
  public String toString() {
    return "store " + dst + " := " + src;
  }

}