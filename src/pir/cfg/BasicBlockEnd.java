package pir.cfg;

import java.util.Set;

import pir.statement.Statement;

abstract public class BasicBlockEnd extends Statement {

  public abstract Set<BasicBlock> getJumpDst();
}
