package evl.cfg;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;

import util.ssa.BaseGraph;
import util.ssa.BbEdge;

import common.ElementInfo;

import evl.EvlBase;

public class BasicBlockList extends EvlBase {
  private BasicBlock entry;
  private BasicBlock exit;
  final private Set<BasicBlock> basicBlocks = new HashSet<BasicBlock>();

  public BasicBlockList(ElementInfo info, BasicBlock entry, BasicBlock exit) {
    super(info);
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
  
  public Set<BasicBlock> getAllBbs(){
    Set<BasicBlock> ret = new HashSet<BasicBlock>(basicBlocks);
    ret.add(entry);
    ret.add(exit);
    return ret;
  }

  public DirectedGraph<BasicBlock, BbEdge> makeFuncGraph() {
    DirectedGraph<BasicBlock, BbEdge> g = new BaseGraph<BasicBlock, BbEdge>();

    g.addVertex(entry);
    g.addVertex(exit);
    for (BasicBlock bb : basicBlocks) {
      g.addVertex(bb);
    }

    for (BasicBlock u : g.vertexSet()) {
      BasicBlockEnd bbe = u.getEnd();
      for (BasicBlock v : bbe.getJumpDst()) {
        assert (g.vertexSet().contains(v));
        BbEdge e = new BbEdge(u, v);
        g.addEdge(u, v, e);
      }
    }

    return g;
  }
}
