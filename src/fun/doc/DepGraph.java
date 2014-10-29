package fun.doc;

import util.SimpleGraph;
import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.BaseRef;

public class DepGraph extends DefTraverser<Void, Fun> {
  final private SimpleGraph<Fun> g = new SimpleGraph<Fun>();

  static public SimpleGraph<Fun> build(Fun root) {
    DepGraph depGraph = new DepGraph();
    depGraph.traverse(root, root);
    return depGraph.g;
  }

  public SimpleGraph<Fun> getGraph() {
    return g;
  }

  @Override
  protected Void visit(Fun obj, Fun param) {
    boolean visited = g.containsVertex(obj);
    g.addVertex(obj);
    g.addEdge(param, obj);
    if (visited) {
      return null;
    }
    return super.visit(obj, obj);
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Fun param) {
    super.visitBaseRef(obj, param);
    visit(obj.getLink(), obj);
    return null;
  }

}
