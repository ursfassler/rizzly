package evl.statement.normal;

import common.ElementInfo;

import evl.variable.FuncVariable;

public class VarDefStmt extends NormalStmt {
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
