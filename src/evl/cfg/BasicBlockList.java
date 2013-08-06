package evl.cfg;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import evl.EvlBase;

public class BasicBlockList extends EvlBase {
  private List<BasicBlock> bbs = new ArrayList<BasicBlock>();

  public BasicBlockList(ElementInfo info) {
    super(info);
  }

  public List<BasicBlock> getBasicBlocks() {
    return bbs;
  }

}
