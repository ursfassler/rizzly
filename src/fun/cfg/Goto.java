package fun.cfg;

import java.util.HashSet;
import java.util.Set;

import common.ElementInfo;

import fun.FunBase;

public class Goto extends FunBase implements BasicBlockEnd{
  private BasicBlock target;

  public Goto(ElementInfo info, BasicBlock target) {
    super(info);
    this.target = target;
  }

  public BasicBlock getTarget() {
    return target;
  }

  public void setTarget(BasicBlock target) {
    this.target = target;
  }

  @Override
  public String toString() {
    return "Goto:" + target;
  }

  @Override
  public Set<BasicBlock> getJumpDst() {
    Set<BasicBlock> ret = new HashSet<BasicBlock>();
    ret.add(target);
    return ret;
  }

}
