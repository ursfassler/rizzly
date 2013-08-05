/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package util.ssa;

public class SimpleEdge<V> implements Edge<V> {
  final private V src;
  final private V dst;

  public SimpleEdge(V src, V dst) {
    super();
    this.src = src;
    this.dst = dst;
  }

  public V getSrc() {
    return src;
  }

  public V getDst() {
    return dst;
  }

  @Override
  public String toString() {
    return src + " - " + dst;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dst == null) ? 0 : dst.hashCode());
    result = prime * result + ((src == null) ? 0 : src.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SimpleEdge<?> other = (SimpleEdge<?>) obj;
    if (dst == null) {
      if (other.dst != null) return false;
    } else if (!dst.equals(other.dst)) return false;
    if (src == null) {
      if (other.src != null) return false;
    } else if (!src.equals(other.src)) return false;
    return true;
  }

}
