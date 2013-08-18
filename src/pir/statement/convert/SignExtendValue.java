package pir.statement.convert;

import pir.expression.PExpression;
import pir.other.Variable;

final public class SignExtendValue extends ConvertValue {

  public SignExtendValue(Variable variable, PExpression original) {
    super(variable, original);
  }

  @Override
  public String toString() {
    return getVariable() + " := sext " + getOriginal();
  }

}
