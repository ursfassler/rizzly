package pir.statement;

import pir.other.PirValue;
import pir.other.SsaVariable;

public class Assignment extends VariableGeneratorStmt {
  private PirValue src;

  public Assignment(SsaVariable dst, PirValue src) {
    super(dst);
    this.src = src;
  }

  public PirValue getSrc() {
    return src;
  }

  public void setSrc(PirValue src) {
    this.src = src;
  }

  @Override
  public String toString() {
    return getVariable() + " := " + getSrc();
  }

}
