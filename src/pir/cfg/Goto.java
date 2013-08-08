package pir.cfg;

import java.util.HashSet;
import java.util.Set;

public class Goto extends BasicBlockEnd {
  private BasicBlock target;

  public Goto(BasicBlock target) {
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
