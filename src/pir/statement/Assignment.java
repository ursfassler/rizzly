package pir.statement;

import pir.other.Variable;

public class Assignment extends VariableGeneratorStmt {
  private Variable src;

  public Assignment(Variable dst, Variable src) {
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
