/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package util.ssa;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;

import org.jgrapht.DirectedGraph;

/**
 * Computes the dominance frontiers out of an graph and intermediate dominator information. Uses the algorithm from the
 * book "engineering a compiler" by Keith D. Cooper and Linda Torczon
 *
 * @author urs
 *
 * @param <V>
 * @param <E>
 */

public class DominanceFrontier<V, E> {
  private DirectedGraph<V, E>    g;
  private HashMap<V, V>          idom;
  private HashMap<V, HashSet<V>> df = new HashMap<V, HashSet<V>>();

  public DominanceFrontier(DirectedGraph<V, E> g, HashMap<V, V> idom) {
    super();
    this.g = g;
    this.idom = idom;
  }

  public void calc() {
    for (V n : g.vertexSet()) {
      df.put(n, new HashSet<V>());
    }

    for (V n : g.vertexSet()) {
      if (g.inDegreeOf(n) > 1) {
        for (E e : g.incomingEdgesOf(n)) {
          V p = g.getEdgeSource(e);
          while (p != idom.get(n)) {
            df.get(p).add(n);
            p = idom.get(p);
          }
        }
      }
    }
  }

  public HashMap<V, HashSet<V>> getDf() {
    return df;
  }

  public void dump(PrintStream out) {
    out.println("Dominance Frontier");
    for (V v : g.vertexSet()) {
      out.print(v);
      out.print(":");
      for (V u : df.get(v)) {
        out.print(" ");
        out.print(u);
      }
      out.println();
    }
  }
}
