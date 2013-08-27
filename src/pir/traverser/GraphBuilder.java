package pir.traverser;

import org.jgrapht.graph.SimpleDirectedGraph;

import pir.DefTraverser;
import pir.PirObject;
import util.Pair;
import util.SimpleGraph;

//TODO used?
public class GraphBuilder extends DefTraverser<Void, PirObject> {
  private SimpleDirectedGraph<PirObject, Pair<PirObject, PirObject>> g =  new SimpleGraph<PirObject>();

  static public SimpleDirectedGraph<PirObject, Pair<PirObject, PirObject>> make( PirObject obj ){
    GraphBuilder builder = new GraphBuilder();
    builder.traverse(obj, obj);
    return builder.getGraph();
  }

  public SimpleDirectedGraph<PirObject, Pair<PirObject, PirObject>> getGraph() {
    return g;
  }

  @Override
  protected Void visit(PirObject obj, PirObject parent) {
    g.addEdge(parent, obj);
    super.visit(obj, obj);
    return null;
  }

  @Override
  protected Void visitRefHead(RefHead obj, PirObject param) {
    g.addEdge(obj, (PirObject) obj.getRef());
    return null;
  }

}
