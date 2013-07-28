package fun.doc.compgraph;

import java.util.ArrayList;

import util.Point;

import common.Designator;

final public class SubComponent extends Component {
  final private String instname;
  final private Point pos = new Point();

  public SubComponent(String instname, Designator path, String classname, String metadata) {
    super(path, classname, metadata);
    this.instname = instname;
  }

  public String getInstname() {
    return instname;
  }

  public Point getSize() {
    Point size = new Point();

    size.x = SUBCOMP_WIDTH;
    size.y = Math.max(input.size(), output.size()) * Y_IFACE_DIST + Y_SUBC_IFACE_OFFSET;

    return size;
  }

  public ArrayList<Connection> getInEdges() {
    ArrayList<Connection> ret = new ArrayList<Connection>();
    for (Interface iface : input) {
      ret.addAll(iface.getConnection());
    }
    return ret;
  }

  public ArrayList<Connection> getOutEdges() {
    ArrayList<Connection> ret = new ArrayList<Connection>();
    for (Interface iface : output) {
      ret.addAll(iface.getConnection());
    }
    return ret;
  }

  @Override
  public Point getPos() {
    return pos;
  }

  @Override
  public String toString() {
    return instname;
  }

  @Override
  public Point getSrcPort(Connection con) {
    assert (getOutEdges().contains(con));
    int x;
    int index;
    x = SUBCOMP_WIDTH / 2;
    index = output.indexOf(con.getSrc());
    assert (index >= 0);
    int y = pos.y + index * Y_IFACE_DIST + Y_SUBC_IFACE_OFFSET;
    y += con.getSrc().getYOffset(con);
    return new Point(pos.x + x, y);
  }

  @Override
  public Point getDstPort(Connection con) {
    assert (getInEdges().contains(con));
    int x;
    int index;
    x = -SUBCOMP_WIDTH / 2;
    index = input.indexOf(con.getDst());
    assert (index >= 0);
    int y = pos.y + index * Y_IFACE_DIST + Y_SUBC_IFACE_OFFSET;
    y += con.getDst().getYOffset(con);
    return new Point(pos.x + x, y);
  }
}
