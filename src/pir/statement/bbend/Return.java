package pir.statement.bbend;

import java.util.HashSet;
import java.util.Set;
import pir.cfg.BasicBlock;

/**
 *
 * @author urs
 */
abstract public class Return extends BasicBlockEnd {

  @Override
  public String toString() {
    return "return";
  }

  @Override
  public Set<BasicBlock> getJumpDst() {
    return new HashSet<BasicBlock>();
  }
}
