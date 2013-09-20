package evl.statement.bbend;

import java.util.HashSet;
import java.util.Set;

import common.ElementInfo;

import evl.cfg.BasicBlock;

/**
 *
 * @author urs
 */
public class Unreachable extends BasicBlockEnd {

  public Unreachable(ElementInfo info) {
    super(info);
  }

  @Override
  public Set<BasicBlock> getJumpDst() {
    return new HashSet<BasicBlock>();
  }
}
