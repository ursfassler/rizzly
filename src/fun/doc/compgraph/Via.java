package fun.doc.compgraph;

import util.PointF;

public class Via implements Vertex {
  private PointF pos;

  public Via(PointF pos) {
    super();
    this.pos = pos;
  }

  @Override
  public PointF getPos() {
    return pos;
  }

}
