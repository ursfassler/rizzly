package fun.doc.compgraph;

import java.util.LinkedList;

public class Connection {
  final private Interface src;
  final private Interface dst;
  final private LinkedList<Vertex> vias = new LinkedList<Vertex>();
  final protected String metadata;

  public Connection(Interface src, Interface dst, String metadata) {
    super();
    this.src = src;
    this.dst = dst;
    this.metadata = metadata;
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

  public String getMetadata() {
    return metadata;
  }

  @Override
  public String toString() {
    return src + " -> " + dst;
  }

}
