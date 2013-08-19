package pir.statement;

import pir.other.FuncVariable;

final public class VarDefStmt extends Statement {
  final private FuncVariable variable;

  public VarDefStmt(FuncVariable variable) {
    super();
    this.variable = variable;
  }

  public FuncVariable getVariable() {
    return variable;
  }

  @Override
  public String toString() {
    return variable.toString();
  }
  
  

}
