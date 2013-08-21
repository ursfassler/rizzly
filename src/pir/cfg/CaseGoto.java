package pir.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pir.other.PirValue;

public class CaseGoto extends BasicBlockEnd {
  private PirValue condition;
  private List<CaseGotoOpt> option = new ArrayList<CaseGotoOpt>();
  private BasicBlock otherwise;

  public PirValue getCondition() {
    return condition;
  }

  public void setCondition(PirValue condition) {
    this.condition = condition;
  }

  public BasicBlock getOtherwise() {
    return otherwise;
  }

  public void setOtherwise(BasicBlock otherwise) {
    this.otherwise = otherwise;
  }

  public List<CaseGotoOpt> getOption() {
    return option;
  }

  @Override
  public Set<BasicBlock> getJumpDst() {
    Set<BasicBlock> ret = new HashSet<BasicBlock>();
    for (CaseGotoOpt cgo : option) {
      ret.add(cgo.getDst());
    }
    ret.add(otherwise);
    return ret;
  }

}
