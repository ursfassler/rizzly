package pir.statement.convert;

import pir.other.PirValue;
import pir.other.SsaVariable;
import pir.statement.VariableGeneratorStmt;

abstract public class ConvertValue extends VariableGeneratorStmt {
  private PirValue original;

  public ConvertValue(SsaVariable variable, PirValue original) {
    super(variable);
    this.original = original;
  }

  public PirValue getOriginal() {
    return original;
  }

  public void setOriginal(PirValue original) {
    this.original = original;
  }


}
