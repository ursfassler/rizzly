package util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;

public class SimpleGraph<V> implements DirectedGraph<V, Pair<V, V>> {
  final private Map<V, Set<V>> outlist = new HashMap<V, Set<V>>();

  public SimpleGraph() {
  }

  public SimpleGraph(Collection<V> vertices) {
    for (V v : vertices) {
      outlist.put(v, new HashSet<V>());
    }
  }

  public Set<V> getOutVertices(V u) {
    assert (containsVertex(u));
    return new HashSet<V>(outlist.get(u));
  }

  @Override
  public Pair<V, V> addEdge(V u, V v) {
    assert (containsVertex(u));
    assert (containsVertex(v));
    outlist.get(u).add(v);
    return new Pair<V, V>(u, v);
  }

  @Override
  public boolean addEdge(V arg0, V arg1, Pair<V, V> arg2) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public boolean addVertex(V arg0) {
    if (!containsVertex(arg0)) {
      outlist.put(arg0, new HashSet<V>());
    }
    return true;
  }

  @Override
  public boolean containsEdge(Pair<V, V> arg0) {
    return outlist.get(arg0.first).contains(arg0.second);
  }

  @Override
  public boolean containsVertex(V arg0) {
    return outlist.containsKey(arg0);
  }

  @Override
  public Set<Pair<V, V>> edgeSet() {
    Set<Pair<V, V>> ret = new HashSet<Pair<V, V>>();
    for (V u : outlist.keySet()) {
      ret.addAll(outgoingEdgesOf(u));
    }
    return ret;
  }

  @Override
  public Set<Pair<V, V>> edgesOf(V arg0) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public Set<Pair<V, V>> getAllEdges(V arg0, V arg1) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public Pair<V, V> getEdge(V arg0, V arg1) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public EdgeFactory<V, Pair<V, V>> getEdgeFactory() {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public V getEdgeSource(Pair<V, V> e) {
    return e.first;
  }

  @Override
  public V getEdgeTarget(Pair<V, V> e) {
    return e.second;
  }

  @Override
  public double getEdgeWeight(Pair<V, V> arg0) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public boolean removeEdge(Pair<V, V> e) {
    removeEdge(e.first, e.second);
    return true;
  }

  @Override
  public Pair<V, V> removeEdge(V u, V v) {
    assert (containsVertex(u));
    assert (containsVertex(v));
    // assert( containsEdge(u, v) );
    outlist.get(u).remove(v);
    return new Pair<V, V>(u, v);
  }

  @Override
  public boolean removeVertex(V arg0) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public Set<V> vertexSet() {
    return new HashSet<V>(outlist.keySet());
  }

  @Override
  public boolean containsEdge(V u, V v) {
    return outlist.get(u).contains(v);
  }

  @Override
  public boolean removeAllEdges(Collection<? extends Pair<V, V>> arg0) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public Set<Pair<V, V>> removeAllEdges(V arg0, V arg1) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public boolean removeAllVertices(Collection<? extends V> arg0) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public int inDegreeOf(V v) {
    int ret = 0;

    for (V u : outlist.keySet()) {
      if (outlist.get(u).contains(v)) {
        ret++;
      }
    }

    return ret;
  }

  @Override
  public Set<Pair<V, V>> incomingEdgesOf(V v) {
    Set<Pair<V, V>> ret = new HashSet<Pair<V, V>>();

    for (Pair<V, V> e : edgeSet()) {
      if (e.second == v) {
        ret.add(e);
      }
    }

    return ret;
  }

  @Override
  public int outDegreeOf(V arg0) {
    return outlist.get(arg0).size();
  }

  @Override
  public Set<Pair<V, V>> outgoingEdgesOf(V u) {
    Set<Pair<V, V>> ret = new HashSet<Pair<V, V>>();
    for (V v : outlist.get(u)) {
      ret.add(new Pair<V, V>(u, v));
    }
    return ret;
  }

}
