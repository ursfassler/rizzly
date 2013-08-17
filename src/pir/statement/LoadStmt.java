package pir.statement;

import pir.other.StateVariable;
import pir.other.Variable;

public class LoadStmt extends VariableGeneratorStmt {
  private StateVariable src;

  public LoadStmt(Variable variable, StateVariable src) {
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
