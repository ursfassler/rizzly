package evl.statement;

import common.ElementInfo;

import evl.expression.Expression;
import evl.variable.SsaVariable;

public class VarDefInitStmt extends Statement {
  private SsaVariable variable;
  private Expression init;

  public VarDefInitStmt(ElementInfo info, SsaVariable variable, Expression init) {
    super(info);
    this.variable = variable;
    this.init = init;
  }

  public SsaVariable getVariable() {
    return variable;
  }

  @Override
  public String toString() {
    return variable.toString();
  }

  public Expression getInit() {
    return init;
  }

  public void setInit(Expression init) {
    this.init = init;
  }

}
