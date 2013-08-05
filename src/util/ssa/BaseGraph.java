/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package util.ssa;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;

public class BaseGraph<V, E extends Edge<V>> implements DirectedGraph<V, E> {
  Set<V> vertices = new HashSet<V>();
  Set<E> edges    = new HashSet<E>();

  public void addEdge(E e) {
    assert (vertices.contains(e.getSrc()));
    assert (vertices.contains(e.getDst()));
    edges.add(e);
  }


  public E addEdge(V arg0, V arg1) {
    throw new RuntimeException("Not yet implemented");
  }


  public boolean addEdge(V arg0, V arg1, E arg2) {
    addEdge( arg2 );
    return true;
  }


  public boolean addVertex(V arg0) {
    return vertices.add(arg0);
  }


  public boolean containsEdge(E arg0) {
    throw new RuntimeException("Not yet implemented");
  }


  public boolean containsEdge(V arg0, V arg1) {
    throw new RuntimeException("Not yet implemented");
  }


  public boolean containsVertex(V arg0) {
    throw new RuntimeException("Not yet implemented");
  }


  public Set<E> edgeSet() {
    return edges;
  }


  public Set<E> edgesOf(V arg0) {
    throw new RuntimeException("Not yet implemented");
  }


  public Set<E> getAllEdges(V arg0, V arg1) {
    throw new RuntimeException("Not yet implemented");
  }


  public E getEdge(V arg0, V arg1) {
    throw new RuntimeException("Not yet implemented");
  }


  public EdgeFactory<V, E> getEdgeFactory() {
    throw new RuntimeException("Not yet implemented");
  }


  public V getEdgeSource(E arg0) {
    return arg0.getSrc();
  }


  public V getEdgeTarget(E arg0) {
    return arg0.getDst();
  }


  public double getEdgeWeight(E arg0) {
    throw new RuntimeException("Not yet implemented");
  }


  public boolean removeAllEdges(Collection<? extends E> arg0) {
    throw new RuntimeException("Not yet implemented");
  }


  public Set<E> removeAllEdges(V arg0, V arg1) {
    throw new RuntimeException("Not yet implemented");
  }


  public boolean removeAllVertices(Collection<? extends V> arg0) {
    throw new RuntimeException("Not yet implemented");
  }


  public boolean removeEdge(E arg0) {
    throw new RuntimeException("Not yet implemented");
  }


  public E removeEdge(V arg0, V arg1) {
    throw new RuntimeException("Not yet implemented");
  }


  public boolean removeVertex(V arg0) {
    throw new RuntimeException("Not yet implemented");
  }


  public Set<V> vertexSet() {
    return vertices;
  }


  public int inDegreeOf(V arg0) {
    return incomingEdgesOf(arg0).size();
  }


  public Set<E> incomingEdgesOf(V arg0) {
    HashSet<E> res = new HashSet<E>();
    for (E e : edges) {
      if (e.getDst() == arg0) {
        res.add(e);
      }
    }
    return res;
  }


  public int outDegreeOf(V arg0) {
    return outgoingEdgesOf(arg0).size();
  }


  public Set<E> outgoingEdgesOf(V arg0) {
    HashSet<E> res = new HashSet<E>();
    for (E e : edges) {
      if (e.getSrc() == arg0) {
        res.add(e);
      }
    }
    return res;
  }

}
