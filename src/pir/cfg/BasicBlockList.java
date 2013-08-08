package pir.cfg;

import java.util.ArrayList;
import java.util.List;

import pir.PirObject;

import common.ElementInfo;


public class BasicBlockList extends PirObject {
  private List<BasicBlock> bbs = new ArrayList<BasicBlock>();

  public List<BasicBlock> getBasicBlocks() {
    return bbs;
  }

}
