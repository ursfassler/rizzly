package evl.traverser;

import java.util.Set;

import util.SimpleGraph;
import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;

public class DepGraph extends DefTraverser<Void, Evl> {
  final private SimpleGraph<Evl> g = new SimpleGraph<Evl>();

  static public SimpleGraph<Evl> build(Evl evl) {
    DepGraph depGraph = new DepGraph();
    depGraph.traverse(evl, evl);
    return depGraph.g;
  }

  public static SimpleGraph<Evl> build(Set<? extends Evl> roots) {
    DepGraph depGraph = new DepGraph();
    for (Evl itr : roots) {
      depGraph.traverse(itr, itr);
    }
    return depGraph.g;
  }

  @Override
  protected Void visit(Evl obj, Evl param) {
    boolean visited = g.containsVertex(obj);
    g.addVertex(obj);
    g.addEdge(param, obj);
    if (visited) {
      return null;
    }
    return super.visit(obj, obj);
  }

  @Override
  protected Void visitTypeRef(SimpleRef obj, Evl param) {
    super.visitTypeRef(obj, param);
    visit(obj.getLink(), obj);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Evl param) {
    super.visitReference(obj, param);
    visit(obj.getLink(), obj);
    return null;
  }
}
