package fun.cfg;

import java.util.Set;

import fun.Fun;

public interface BasicBlockEnd extends Fun {
  public Set<BasicBlock> getJumpDst();
}
