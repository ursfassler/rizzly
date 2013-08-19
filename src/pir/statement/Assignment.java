package pir.statement;

import pir.expression.reference.VarRef;
import pir.other.Variable;

public class Assignment extends VariableGeneratorStmt {
  private VarRef src;

  public Assignment(Variable dst, VarRef src) {
    super(dst);
    this.src = src;
  }

  public VarRef getSrc() {
    return src;
  }

  public void setSrc(VarRef src) {
    this.src = src;
  }

  @Override
  public String toString() {
    return getVariable() + " := " + getSrc();
  }

}
