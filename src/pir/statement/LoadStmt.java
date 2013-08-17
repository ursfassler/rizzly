package pir.statement;

import pir.other.SsaVariable;
import pir.other.StateVariable;

public class LoadStmt extends VariableGeneratorStmt {
  private StateVariable src;

  public LoadStmt(SsaVariable variable, StateVariable src) {
    super(variable);
    this.src = src;
  }

  public StateVariable getSrc() {
    return src;
  }

  public void setSrc(StateVariable src) {
    this.src = src;
  }

  @Override
  public String toString() {
    return super.toString() + " := load " + src;
  }

}
