package fun.doc.compgraph;

import java.util.ArrayList;
import java.util.List;

import util.Point;

import common.Designator;
import common.Metadata;

final public class SubComponent extends Component {
  private static final Point ErrorPoint = new Point(0, 0);
  final private String instname;
  final private Point pos = new Point();

  public SubComponent(String instname, Designator path, String classname, List<Metadata> metadata) {
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
    if (!getOutEdges().contains(con)) {
      return ErrorPoint;
    }
    int x;
    int index;
    x = SUBCOMP_WIDTH / 2;
    index = output.indexOf(con.getSrc());
    if (index < 0) {
      return ErrorPoint;
    }
    int y = pos.y + index * Y_IFACE_DIST + Y_SUBC_IFACE_OFFSET;
    int yOffset = con.getSrc().getYOffset(con);
    if (yOffset < 0) {
      return ErrorPoint;
    }
    y += yOffset;
    return new Point(pos.x + x, y);
  }

  @Override
  public Point getDstPort(Connection con) {
    if (!getInEdges().contains(con)) {
      return ErrorPoint;
    }
    int x;
    int index;
    x = -SUBCOMP_WIDTH / 2;
    index = input.indexOf(con.getDst());
    if (index < 0) {
      return ErrorPoint;
    }
    int y = pos.y + index * Y_IFACE_DIST + Y_SUBC_IFACE_OFFSET;
    int yOffset = con.getDst().getYOffset(con);
    if (yOffset < 0) {
      return ErrorPoint;
    }
    y += yOffset;
    return new Point(pos.x + x, y);
  }
}
