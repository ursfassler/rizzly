package evl.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jgrapht.DirectedGraph;

import util.ssa.BaseGraph;
import util.ssa.BbEdge;

import common.ElementInfo;

import evl.EvlBase;
import evl.statement.Statement;

public class BasicBlock extends EvlBase {
  final private int id;
  final private List<PhiStmt> phi = new ArrayList<PhiStmt>();
  final private List<Statement> code = new ArrayList<Statement>();
  private BasicBlockEnd end = null;

  public BasicBlock(ElementInfo info, int id) {
    super(info);
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public List<Statement> getCode() {
    return code;
  }

  @Override
  public String toString() {
    return "BB_" + id;
  }

  public BasicBlockEnd getEnd() {
    return end;
  }

  public void setEnd(BasicBlockEnd end) {
    this.end = end;
  }

  public List<PhiStmt> getPhi() {
    return phi;
  }

  public static DirectedGraph<BasicBlock, BbEdge> makeFuncGraph(Collection<BasicBlock> bbs) {
    DirectedGraph<BasicBlock, BbEdge> g = new BaseGraph<BasicBlock, BbEdge>();

    for (BasicBlock u : bbs) {
      g.addVertex(u);
      BasicBlockEnd bbe = u.getEnd();
      for (BasicBlock v : bbe.getJumpDst()) {
        g.addVertex(v);
        BbEdge e = new BbEdge(u, v);
        g.addEdge(u, v, e);
      }
    }

    return g;
  }
}
