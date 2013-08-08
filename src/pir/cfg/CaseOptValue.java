package pir.cfg;

import java.math.BigInteger;


public class CaseOptValue extends CaseOptEntry {
  private BigInteger value;

  public CaseOptValue( BigInteger value) {
    this.value = value;
  }

  public BigInteger getValue() {
    return value;
  }

  public void setValue(BigInteger value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
