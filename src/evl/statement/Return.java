package evl.statement;

import java.util.HashSet;
import java.util.Set;

import common.ElementInfo;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockEnd;

/**
 *
 * @author urs
 */
abstract public class Return extends Statement implements BasicBlockEnd {

  public Return(ElementInfo info) {
    super(info);
  }

  @Override
  public String toString() {
    return "return";
  }

  @Override
  public Set<BasicBlock> getJumpDst() {
    return new HashSet<BasicBlock>();
  }
}
