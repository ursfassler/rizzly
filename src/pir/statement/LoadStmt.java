package pir.statement;

import pir.other.Variable;

public class LoadStmt extends VariableGeneratorStmt {
  private Variable src;

  public LoadStmt(Variable variable, Variable src) {
    super(variable);
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
    return super.toString() + " := load " + src;
  }

}
