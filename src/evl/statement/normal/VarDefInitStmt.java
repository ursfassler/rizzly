package evl.statement.normal;

import common.ElementInfo;

import evl.expression.Expression;
import evl.variable.SsaVariable;

public class VarDefInitStmt extends NormalStmt implements SsaGenerator {
  private SsaVariable variable;
  private Expression init;

  public VarDefInitStmt(ElementInfo info, SsaVariable variable, Expression init) {
    super(info);
    this.variable = variable;
    this.init = init;
  }

  @Override
  public SsaVariable getVariable() {
    return variable;
  }

  @Override
  public String toString() {
    return variable.toString() + " := " + init.toString();
  }

  public Expression getInit() {
    return init;
  }

  public void setInit(Expression init) {
    this.init = init;
  }

}
