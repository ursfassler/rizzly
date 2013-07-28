package fun.doc.compgraph;

import util.Point;

public class Via implements Vertex {
  private Point pos;

  public Via(Point pos) {
    super();
    this.pos = pos;
  }

  @Override
  public Point getPos() {
    return pos;
  }

}
