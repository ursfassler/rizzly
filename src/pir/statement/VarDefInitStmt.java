package pir.statement;

import pir.expression.PExpression;
import pir.other.SsaVariable;


public class VarDefInitStmt extends Statement {
  private SsaVariable variable;
  private PExpression init;

  public VarDefInitStmt( SsaVariable variable, PExpression init) {
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

  public PExpression getInit() {
    return init;
  }

  public void setInit(PExpression init) {
    this.init = init;
  }

}
