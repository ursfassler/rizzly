package evl.statement.bbend;

import java.util.List;

import common.ElementInfo;

import evl.EvlBase;
import evl.cfg.BasicBlock;

public class CaseGotoOpt extends EvlBase {
  private List<CaseOptEntry> value;
  private BasicBlock dst;

  public CaseGotoOpt(ElementInfo info, List<CaseOptEntry> value, BasicBlock dst) {
    super(info);
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
