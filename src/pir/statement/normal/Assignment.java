package pir.statement.normal;

import pir.other.PirValue;
import pir.other.SsaVariable;

public class Assignment extends NormalStmt implements VariableGeneratorStmt {
  private SsaVariable dst;
  private PirValue src;

  public Assignment(SsaVariable dst, PirValue src) {
    this.dst = dst;
    this.src = src;
  }

  @Override
  public SsaVariable getVariable() {
    return dst;
  }

  @Override
  public void setVariable(SsaVariable variable) {
    this.dst = variable;
  }

  public PirValue getSrc() {
    return src;
  }

  public void setSrc(PirValue src) {
    this.src = src;
  }

  @Override
  public String toString() {
    return dst + " := " + getSrc();
  }

}
