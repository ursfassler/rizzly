package util;

public class GraphHelper {

  // TODO do it more elegant
  static public <T> void doTransitiveClosure(util.SimpleGraph<T> cg) {
    for (T a : cg.vertexSet()) {
      int outdeg = 0;
      while (outdeg != cg.outDegreeOf(a)) {
        outdeg = cg.outDegreeOf(a);
        for (T b : cg.getOutVertices(a)) {
          for (T c : cg.getOutVertices(b)) {
            cg.addEdge(a, c);
          }
        }
      }
    }
  }

}
