package pir.statement;

import pir.other.SsaVariable;

abstract public class VariableGeneratorStmt extends Statement {
  private SsaVariable variable;

  public VariableGeneratorStmt(SsaVariable variable) {
    this.variable = variable;
  }

  public SsaVariable getVariable() {
    return variable;
  }

  public void setVariable(SsaVariable variable) {
    this.variable = variable;
  }

  @Override
  public String toString() {
    return variable.toString();
  }

}
