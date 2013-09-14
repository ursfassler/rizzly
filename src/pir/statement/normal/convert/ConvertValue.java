package pir.statement.normal.convert;

import pir.other.PirValue;
import pir.other.SsaVariable;
import pir.statement.normal.NormalStmt;
import pir.statement.normal.VariableGeneratorStmt;

abstract public class ConvertValue extends NormalStmt implements VariableGeneratorStmt {

  private SsaVariable variable;
  private PirValue original;

  public ConvertValue(SsaVariable variable, PirValue original) {
    this.variable = variable;
    this.original = original;
  }

  @Override
  public SsaVariable getVariable() {
    return variable;
  }

  @Override
  public void setVariable(SsaVariable variable) {
    this.variable = variable;
  }

  public PirValue getOriginal() {
    return original;
  }

  public void setOriginal(PirValue original) {
    this.original = original;
  }
}
