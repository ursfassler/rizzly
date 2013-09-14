package pir.statement.bbend;

import pir.other.PirValue;

/**
 * 
 * @author urs
 */
public class ReturnExpr extends Return {
  private PirValue value;

  public ReturnExpr(PirValue value) {
    this.value = value;
  }

  public PirValue getValue() {
    return value;
  }

  public void setValue(PirValue value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return super.toString() + " " + value;
  }
}
