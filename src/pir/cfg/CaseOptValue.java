package pir.cfg;

import pir.expression.Number;

public class CaseOptValue extends CaseOptEntry {
  private Number value;

  public CaseOptValue(Number value) {
    this.value = value;
  }

  public Number getValue() {
    return value;
  }

  public void setValue(Number value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
