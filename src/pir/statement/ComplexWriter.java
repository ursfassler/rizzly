package pir.statement;

import pir.expression.reference.VarRef;
import pir.other.PirValue;

public class ComplexWriter extends Statement {
  private VarRef dst;
  private PirValue src;

  public ComplexWriter(VarRef dst, PirValue src) {
    this.dst = dst;
    this.src = src;
  }

  public PirValue getSrc() {
    return src;
  }

  public void setSrc(PirValue src) {
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
