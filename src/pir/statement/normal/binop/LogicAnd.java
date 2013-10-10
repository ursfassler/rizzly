package pir.statement.normal.binop;

import pir.other.PirValue;
import pir.other.SsaVariable;

public class LogicAnd extends Logical{

  public LogicAnd(SsaVariable variable, PirValue left, PirValue right) {
    super(variable, left, right);
  }

  @Override
  public String getOpName() {
    return "and";
  }
}
