package util;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.SimpleDirectedGraph;

public class SimpleGraph<T> extends SimpleDirectedGraph<T, Pair<T, T>> {

  public SimpleGraph() {
    super(new EdgeFactory<T, Pair<T, T>>() {
      @Override
      public Pair<T, T> createEdge(T arg0, T arg1) {
        return new Pair<T, T>(arg0, arg1);
      }
    });
  }

  public Set<T> getOutVertices(T u) {
    Set<T> ret = new HashSet<T>();

    for (Pair<T, T> e : outgoingEdgesOf(u)) {
      ret.add(e.second);
    }

    return ret;
  }

}
