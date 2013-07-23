package fun.doc.compgraph;

import java.util.LinkedList;

public class Connection {
  final private Interface src;
  final private Interface dst;
  final private LinkedList<Vertex> vias = new LinkedList<Vertex>();

  public Connection(Interface src, Interface dst) {
    super();
    this.src = src;
    this.dst = dst;
  }

  public Interface getSrc() {
    return src;
  }

  public Interface getDst() {
    return dst;
  }

  public LinkedList<Vertex> getVias() {
    return vias;
  }

  @Override
  public String toString() {
    return src + " -> " + dst;
  }

}
