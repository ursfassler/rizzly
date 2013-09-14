package pir.statement.bbend;

import java.util.List;

import pir.PirObject;
import pir.cfg.BasicBlock;

public class CaseGotoOpt extends PirObject {
  private List<CaseOptEntry> value;
  private BasicBlock dst;

  public CaseGotoOpt(List<CaseOptEntry> value, BasicBlock dst) {
    this.value = value;
    this.dst = dst;
  }

  public BasicBlock getDst() {
    return dst;
  }

  public void setDst(BasicBlock dst) {
    this.dst = dst;
  }

  public List<CaseOptEntry> getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
