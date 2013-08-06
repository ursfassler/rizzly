package evl.cfg;

import java.util.Set;

import evl.Evl;

public interface BasicBlockEnd extends Evl {
  public Set<BasicBlock> getJumpDst();
}
