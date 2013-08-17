package pir.traverser;

import pir.DefTraverser;
import pir.PirObject;
import util.SimpleGraph;

//TODO used?
public class GraphBuilder extends DefTraverser<Void, PirObject> {
  private SimpleGraph<PirObject> g = new SimpleGraph<PirObject>();

  static public SimpleGraph<PirObject> make( PirObject obj ){
    GraphBuilder builder = new GraphBuilder();
    builder.traverse(obj, obj);
    return builder.getGraph();
  }

  public SimpleGraph<PirObject> getGraph() {
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
