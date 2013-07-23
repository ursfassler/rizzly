package util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;


public class SimpleGraph<T> implements DirectedGraph<T, Pair<T, T>> {
  private Map<T, Set<T>> outlist;

  public SimpleGraph(Map<T, Set<T>> outlist) {
    this.outlist = outlist;
  }

  public SimpleGraph() {
    this.outlist = new HashMap<T, Set<T>>();
  }

  private Pair<T, T> edge(T u, T v) {
    return new Pair<T, T>(u, v);
  }

  public Set<T> getOutVertices( T u ){
    return new HashSet<T>( outlist.get(u) );
  }

  @Override
  public Pair<T, T> addEdge(T u, T v) {
    addVertex(u);
    addVertex(v);
    outlist.get(u).add(v);
    return edge(u, v);
  }

  @Override
  public boolean addEdge(T u, T v, Pair<T, T> arg2) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public boolean addVertex(T u) {
    if (!outlist.containsKey(u)) {
      outlist.put(u, new HashSet<T>());
    }
    return true;
  }

  @Override
  public boolean containsEdge(Pair<T, T> e) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public boolean containsEdge(T u, T v) {
    return outlist.get(u).contains(v);
  }

  @Override
  public boolean containsVertex(T u) {
    return outlist.containsKey(u);
  }

  @Override
  public Set<Pair<T, T>> edgeSet() {
    Set<Pair<T, T>> ret = new HashSet<Pair<T, T>>();
    for (T v : outlist.keySet()) {
      Set<T> us = outlist.get(v);
      for (T u : us) {
        ret.add(new Pair<T, T>(v, u));
      }
    }
    return ret;
  }

  @Override
  public Set<Pair<T, T>> edgesOf(T u) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public Set<Pair<T, T>> getAllEdges(T u, T v) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public Pair<T, T> getEdge(T u, T v) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public EdgeFactory<T, Pair<T, T>> getEdgeFactory() {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public T getEdgeSource(Pair<T, T> e) {
    return e.first;
  }

  @Override
  public T getEdgeTarget(Pair<T, T> e) {
    return e.second;
  }

  @Override
  public double getEdgeWeight(Pair<T, T> e) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public boolean removeAllEdges(Collection<? extends Pair<T, T>> arg0) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public Set<Pair<T, T>> removeAllEdges(T u, T v) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public boolean removeAllVertices(Collection<? extends T> arg0) {
    for (T u : arg0) {
      removeVertex(u);
    }
    return true;
  }

  @Override
  public boolean removeEdge(Pair<T, T> e) {
    return removeEdge(e.first, e.second) != null;
  }

  @Override
  public Pair<T, T> removeEdge(T u, T v) {
    Set<T> list = outlist.get(u);
    if (list == null) {
      return null;
    }
    if (list.remove(v)) {
      return new Pair<T, T>(u, v);
    } else {
      return null;
    }
  }

  @Override
  public boolean removeVertex(T u) {
    outlist.remove(u);
    for (Set<T> dst : outlist.values()) {
      dst.remove(u);
    }
    return true;
  }

  @Override
  public Set<T> vertexSet() {
    return new HashSet<T>(outlist.keySet());
  }

  @Override
  public int inDegreeOf(T u) {
    int n = 0;
    for (T itr : outlist.keySet()) {
      if (outlist.get(itr).contains(u)) {
        n++;
      }
    }
    return n;
  }

  @Override
  public Set<Pair<T, T>> incomingEdgesOf(T v) {
    Set<Pair<T, T>> ret = new HashSet<Pair<T, T>>();
    for (T u : outlist.keySet()) {
      if (outlist.get(u).contains(v)) {
        ret.add(edge(u, v));
      }
    }
    return ret;
  }

  @Override
  public int outDegreeOf(T u) {
    return outlist.get(u).size();
  }

  @Override
  public Set<Pair<T, T>> outgoingEdgesOf(T u) {
    Set<Pair<T, T>> ret = new HashSet<Pair<T, T>>();
    for (T v : outlist.get(u)) {
      ret.add(edge(u, v));
    }
    return ret;
  }

}
