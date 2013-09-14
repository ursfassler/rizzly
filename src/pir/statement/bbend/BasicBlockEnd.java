package pir.statement.bbend;

import java.util.Set;

import pir.cfg.BasicBlock;
import pir.statement.Statement;

abstract public class BasicBlockEnd extends Statement {

  public abstract Set<BasicBlock> getJumpDst();
}
