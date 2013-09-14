package pir.statement.normal.convert;

import pir.other.PirValue;
import pir.other.SsaVariable;

final public class ZeroExtendValue extends ConvertValue {

  public ZeroExtendValue(SsaVariable variable, PirValue original) {
    super(variable, original);
  }

  @Override
  public String toString() {
    return getVariable() + " := zext " + getOriginal();
  }

}
