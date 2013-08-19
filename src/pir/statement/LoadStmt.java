package pir.statement;

import pir.expression.reference.VarRef;
import pir.other.Variable;

public class LoadStmt extends VariableGeneratorStmt {
  private VarRef src;

  public LoadStmt(Variable variable, VarRef src) {
    super(variable);
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
    return super.toString() + " := load " + src;
  }

}
