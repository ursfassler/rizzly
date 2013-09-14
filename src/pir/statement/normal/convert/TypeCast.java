package pir.statement.normal.convert;

import pir.other.PirValue;
import pir.other.SsaVariable;

final public class TypeCast extends ConvertValue {

  public TypeCast(SsaVariable variable, PirValue original) {
    super(variable, original);
  }

  @Override
  public String toString() {
    return getVariable() + " := typecast " + getOriginal();
  }

}
