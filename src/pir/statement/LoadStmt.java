package pir.statement;

import pir.other.PirValue;
import pir.other.SsaVariable;

public class LoadStmt extends VariableGeneratorStmt {
  private PirValue src;

  public LoadStmt(SsaVariable variable, PirValue src) {
    super(variable);
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
    return super.toString() + " := load " + src;
  }

}
