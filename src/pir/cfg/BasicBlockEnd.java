package pir.cfg;

import java.util.Set;

import pir.PirObject;

abstract public class BasicBlockEnd extends PirObject {

  public abstract Set<BasicBlock> getJumpDst();
}
