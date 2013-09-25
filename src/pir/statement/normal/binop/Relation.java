package pir.statement.normal.binop;

import pir.other.PirValue;
import pir.other.SsaVariable;

/**
 *
 * @author urs
 */
abstract public class Relation extends BinaryOp {

  public Relation(SsaVariable variable, PirValue left, PirValue right) {
    super(variable, left, right);
  }
  
}
