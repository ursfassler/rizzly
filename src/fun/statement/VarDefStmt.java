package fun.statement;

import common.ElementInfo;

import fun.variable.FuncVariable;

public class VarDefStmt extends Statement {
  private FuncVariable variable;

  public VarDefStmt(ElementInfo info, FuncVariable variable) {
    super(info);
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
