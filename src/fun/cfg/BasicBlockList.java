package fun.cfg;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import fun.FunBase;
import fun.function.FunctionBodyImplementation;

public class BasicBlockList extends FunBase implements FunctionBodyImplementation {
  private List<BasicBlock> bbs = new ArrayList<BasicBlock>();

  public BasicBlockList(ElementInfo info) {
    super(info);
  }

  public List<BasicBlock> getBasicBlocks() {
    return bbs;
  }

  @Override
  public boolean isEmpty() {
    return bbs.isEmpty();
  }

}
