package pir.statement.normal.binop;

import pir.other.PirValue;
import pir.other.SsaVariable;

/**
 *
 * @author urs
 */
public class Mul extends Arithmetic {

  public Mul(SsaVariable variable, PirValue left, PirValue right) {
    super(variable, left, right);
  }

  @Override
  public String getOpName() {
    return "*";
  }
}
