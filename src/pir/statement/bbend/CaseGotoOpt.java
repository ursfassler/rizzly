package pir.statement.bbend;

import pir.PirObject;
import pir.cfg.BasicBlock;
import util.NumberSet;

public class CaseGotoOpt extends PirObject {
  private NumberSet value;
  private BasicBlock dst;

  public CaseGotoOpt(NumberSet value, BasicBlock dst) {
    this.value = value;
    this.dst = dst;
  }

  public BasicBlock getDst() {
    return dst;
  }

  public void setDst(BasicBlock dst) {
    this.dst = dst;
  }

  public NumberSet getValue() {
    return value;
  }

  public void setValue(NumberSet value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
