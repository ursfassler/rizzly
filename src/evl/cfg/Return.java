package evl.cfg;

import java.util.HashSet;
import java.util.Set;

import common.ElementInfo;

/**
 *
 * @author urs
 */
abstract public class Return extends BasicBlockEnd {

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
