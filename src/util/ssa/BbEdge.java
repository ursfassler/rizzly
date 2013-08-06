package util.ssa;

import evl.cfg.BasicBlock;

public class BbEdge extends SimpleEdge<BasicBlock> {

  public BbEdge(BasicBlock src, BasicBlock dst) {
    super(src, dst);
  }

}
