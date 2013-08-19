package pir.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pir.expression.reference.VarRef;

public class CaseGoto extends BasicBlockEnd {
  private VarRef condition;
  private List<CaseGotoOpt> option = new ArrayList<CaseGotoOpt>();
  private BasicBlock otherwise;

  public VarRef getCondition() {
    return condition;
  }

  public void setCondition(VarRef condition) {
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
