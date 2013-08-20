package pir.cfg;

import java.util.HashSet;
import java.util.Set;

import pir.PirObject;

public class BasicBlockList extends PirObject {
  private BasicBlock entry;
  private BasicBlock exit;
  final private Set<BasicBlock> basicBlocks = new HashSet<BasicBlock>();

  public BasicBlockList(BasicBlock entry, BasicBlock exit) {
    super();
    this.entry = entry;
    this.exit = exit;
  }

  public BasicBlock getEntry() {
    return entry;
  }

  public void setEntry(BasicBlock entry) {
    this.entry = entry;
  }

  public BasicBlock getExit() {
    return exit;
  }

  public void setExit(BasicBlock exit) {
    this.exit = exit;
  }

  public Set<BasicBlock> getBasicBlocks() {
    return basicBlocks;
  }

}
