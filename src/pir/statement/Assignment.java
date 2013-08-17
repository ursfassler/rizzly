package pir.statement;

import pir.other.SsaVariable;
import pir.other.Variable;

@Deprecated
public class Assignment extends VariableGeneratorStmt {
  private Variable src;

  public Assignment(SsaVariable dst, Variable src) {
    super(dst);
    this.src = src;
  }

  public Variable getSrc() {
    return src;
  }

  public void setSrc(Variable src) {
    this.src = src;
  }

  @Override
  public String toString() {
    return getVariable() + " := " + getSrc();
  }

}
