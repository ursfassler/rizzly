package pir.statement;

import pir.other.PirValue;

public class StoreStmt extends Statement {
  private PirValue dst;
  private PirValue src;

  public StoreStmt(PirValue dst, PirValue src) {
    this.dst = dst;
    this.src = src;
  }

  public PirValue getSrc() {
    return src;
  }

  public void setSrc(PirValue src) {
    this.src = src;
  }

  public PirValue getDst() {
    return dst;
  }

  public void setDst(PirValue dst) {
    this.dst = dst;
  }

  @Override
  public String toString() {
    return "store " + dst + " := " + src;
  }

}
