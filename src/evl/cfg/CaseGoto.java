package evl.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.ElementInfo;

import evl.expression.Expression;

public class CaseGoto extends BasicBlockEnd {
  private Expression condition;
  private List<CaseGotoOpt> option = new ArrayList<CaseGotoOpt>();
  private BasicBlock otherwise;

  public CaseGoto(ElementInfo info) {
    super(info);
  }

  public Expression getCondition() {
    return condition;
  }

  public void setCondition(Expression condition) {
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
