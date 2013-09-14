package evl.statement.bbend;

import java.util.Set;

import common.ElementInfo;

import evl.cfg.BasicBlock;
import evl.statement.Statement;

abstract public class BasicBlockEnd extends Statement {
  public BasicBlockEnd(ElementInfo info) {
    super(info);
  }

  public abstract Set<BasicBlock> getJumpDst();
}
