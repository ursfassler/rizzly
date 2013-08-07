package evl.cfg;

import java.util.Set;

import common.ElementInfo;

import evl.EvlBase;

abstract public class BasicBlockEnd extends EvlBase {
  public BasicBlockEnd(ElementInfo info) {
    super(info);
  }

  public abstract Set<BasicBlock> getJumpDst();
}
