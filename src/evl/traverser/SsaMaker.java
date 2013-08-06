package evl.traverser;

import org.jgrapht.DirectedGraph;

import util.ssa.BbEdge;
import util.ssa.DominanceFrontier;
import util.ssa.Dominator;
import util.ssa.PhiInserter;
import evl.cfg.BasicBlockList;
import fun.DefGTraverser;
import fun.Fun;
import fun.cfg.BasicBlock;
import fun.function.FuncWithBody;
import fun.function.FunctionHeader;
import fun.knowledge.KnowledgeBase;

public class SsaMaker extends DefGTraverser<Void, Void> {
  private KnowledgeBase kb;

  public SsaMaker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  static public void process(Fun obj, KnowledgeBase kb) {
    SsaMaker mbb = new SsaMaker(kb);
    mbb.traverse(obj, null);
  }

  @Override
  protected Void visitFunctionHeader(FunctionHeader obj, Void param) {
    if (obj instanceof FuncWithBody) {
      BasicBlockList body = (BasicBlockList) ((FuncWithBody) obj).getBody();

      DirectedGraph<BasicBlock, BbEdge> funcGraph = BasicBlock.makeFuncGraph(body.getBasicBlocks());
      Dominator<BasicBlock, BbEdge> dom = new Dominator<BasicBlock, BbEdge>(funcGraph);
      dom.calc();
      DominanceFrontier<BasicBlock, BbEdge> df = new DominanceFrontier<BasicBlock, BbEdge>(funcGraph, dom.getDom());
      df.calc();

      PhiInserter phi = new PhiInserter(obj, df);
      phi.doWork();

      // IntraBbVariableLinker intra = new IntraBbVariableLinker(app.getKb(), linkUnlinked, linkKilled);
      // intra.visit(func, null);
      //
      // InterBbVariableLinker.link(intra, dom.getDom(), func, linkUnlinked, linkKilled);

    }
    return null;
  }

}
