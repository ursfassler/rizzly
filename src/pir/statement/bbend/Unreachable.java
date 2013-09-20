package pir.statement.bbend;

import java.util.HashSet;
import java.util.Set;

import pir.cfg.BasicBlock;

/**
 *
 * @author urs
 */
public class Unreachable extends BasicBlockEnd {

  @Override
  public Set<BasicBlock> getJumpDst() {
    return new HashSet<BasicBlock>();
  }
  
}
