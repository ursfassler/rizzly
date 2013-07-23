package fun.doc.compgraph;

import java.util.ArrayList;

import util.Point;

import common.Designator;

final public class SubComponent extends Component {
  final private String instname;
  final private Point pos = new Point();

  public SubComponent(String instname, Designator path, String classname) {
    super(path,classname);
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
  public Point getPort(Connection con) {
    int x;
    int index;
    if (getInEdges().contains(con)) {
      x = -SUBCOMP_WIDTH / 2;
      index = input.indexOf(con.getDst());
    } else {
      assert (getOutEdges().contains(con));
      x = SUBCOMP_WIDTH / 2;
      index = output.indexOf(con.getSrc());
    }
    assert (index >= 0);
    return new Point(pos.x + x, pos.y + index * Y_IFACE_DIST + Y_SUBC_IFACE_OFFSET);
  }

}
