package pir.statement.normal.binop;

import pir.other.PirValue;
import pir.other.SsaVariable;

public abstract class Logical extends BinaryOp {

  public Logical(SsaVariable variable, PirValue left, PirValue right) {
    super(variable, left, right);
  }
  
  @Override
  public String toString() {
    return super.toString() + " := " + getLeft().toString() + " " + getOpName() + " " + getRight().toString();
  }
}
