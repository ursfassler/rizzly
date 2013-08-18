package pir.statement.convert;

import pir.expression.PExpression;
import pir.other.Variable;

final public class ZeroExtendValue extends ConvertValue {

  public ZeroExtendValue(Variable variable, PExpression original) {
    super(variable, original);
  }

  @Override
  public String toString() {
    return getVariable() + " := zext " + getOriginal();
  }

}
