package pir.statement.convert;

import pir.other.PirValue;
import pir.other.SsaVariable;

final public class SignExtendValue extends ConvertValue {

  public SignExtendValue(SsaVariable variable, PirValue original) {
    super(variable, original);
  }

  @Override
  public String toString() {
    return getVariable() + " := sext " + getOriginal();
  }

}
