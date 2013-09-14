package pir.statement.normal.convert;

import pir.other.PirValue;
import pir.other.SsaVariable;

final public class TruncValue extends ConvertValue {

  public TruncValue(SsaVariable variable, PirValue original) {
    super(variable, original);
  }

  @Override
  public String toString() {
    return getVariable() + " := trunc " + getOriginal();
  }

}
