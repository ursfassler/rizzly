package pir.statement.convert;

import pir.expression.PExpression;
import pir.other.Variable;

final public class TruncValue extends ConvertValue {

  public TruncValue(Variable variable, PExpression original) {
    super(variable, original);
  }

  @Override
  public String toString() {
    return getVariable() + " := trunc " + getOriginal();
  }

}
