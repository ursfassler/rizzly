package pir.statement;

import pir.other.Variable;

abstract public class VariableGeneratorStmt extends Statement {
  private Variable variable;

  public VariableGeneratorStmt(Variable variable) {
    this.variable = variable;
  }

  public Variable getVariable() {
    return variable;
  }

  public void setVariable(Variable variable) {
    this.variable = variable;
  }

  @Override
  public String toString() {
    return variable.toString();
  }

}
