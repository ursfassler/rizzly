package pir.statement.normal;

import pir.other.PirValue;
import pir.other.SsaVariable;

public class LoadStmt extends NormalStmt implements VariableGeneratorStmt {
  private SsaVariable variable;
  private PirValue src;

  public LoadStmt(SsaVariable variable, PirValue src) {
    this.variable = variable;
    this.src = src;
  }

  @Override
  public SsaVariable getVariable() {
    return variable;
  }

  @Override
  public void setVariable(SsaVariable variable) {
    this.variable = variable;
  }

  public PirValue getSrc() {
    return src;
  }

  public void setSrc(PirValue src) {
    this.src = src;
  }

  @Override
  public String toString() {
    return variable + " := load " + src;
  }

}
