package pir.statement.normal;

import pir.Pir;
import pir.other.SsaVariable;

public interface VariableGeneratorStmt extends Pir {

  public SsaVariable getVariable();

  public void setVariable(SsaVariable variable);
}
