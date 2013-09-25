package pir.statement.normal.unop;

import pir.other.PirValue;
import pir.other.SsaVariable;

/**
 *
 * @author urs
 */
public class Not extends UnaryOp {

  public Not(SsaVariable variable, PirValue expr) {
    super(variable, expr);
  }

  @Override
  public String getOpName() {
    return "not";
  }
}
