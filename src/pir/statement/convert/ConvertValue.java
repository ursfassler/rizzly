package pir.statement.convert;

import pir.expression.PExpression;
import pir.other.Variable;
import pir.statement.VariableGeneratorStmt;

abstract public class ConvertValue extends VariableGeneratorStmt {
  private PExpression original;

  public ConvertValue(Variable variable, PExpression original) {
    super(variable);
    this.original = original;
  }

  public PExpression getOriginal() {
    return original;
  }

  public void setOriginal(PExpression original) {
    this.original = original;
  }


}
