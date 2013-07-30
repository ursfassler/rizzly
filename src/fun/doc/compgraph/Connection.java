package fun.doc.compgraph;

import java.util.LinkedList;
import java.util.List;

import common.Metadata;

public class Connection {
  final private Interface src;
  final private Interface dst;
  final private LinkedList<Vertex> vias = new LinkedList<Vertex>();
  final protected List<Metadata> metadata;

  public Connection(Interface src, Interface dst, List<Metadata> metadata) {
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

  public List<Metadata> getMetadata() {
    return metadata;
  }

  @Override
  public String toString() {
    return src + " -> " + dst;
  }

}
