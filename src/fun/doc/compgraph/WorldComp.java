package fun.doc.compgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.Point;

import common.Designator;

public class WorldComp extends Component {
  final private Point size = new Point(140, 35);

  final private List<SubComponent> comp = new ArrayList<SubComponent>();
  final private Set<Connection> conn = new HashSet<Connection>();

  public WorldComp(Designator path, String classname) {
    super(path, classname);
  }

  public List<SubComponent> getComp() {
    return comp;
  }

  public Set<Connection> getConn() {
    return conn;
  }

  public Designator getPath() {
    return path;
  }

  public Point getSize() {
    return size;
  }

  public ArrayList<Connection> getInEdges(){
    ArrayList<Connection> ret = new ArrayList<Connection>();
    for( Interface iface : output ){
      ret.addAll(iface.getConnection());
    }
    return ret;
  }
  
  public ArrayList<Connection> getOutEdges(){
    ArrayList<Connection> ret = new ArrayList<Connection>();
    for( Interface iface : input ){
      ret.addAll(iface.getConnection());
    }
    return ret;
  }
  
  @Override
  public Point getPos() {
    Point pos = new Point();
    pos.x = size.x / 2;
    pos.y = 0;
    return pos;
  }

  @Override
  public String toString() {
    return getClassname();
  }

  @Override
  public Point getPort(Connection con) {
    int x;
    int y;
    if( getInEdges().contains(con) ){
      x = size.x / 2 - Interface.WIDTH;
      int index = output.indexOf(con.getDst());
      assert (index >= 0);
      y = index*Y_IFACE_DIST+Y_WORLD_IFACE_OFFSET;
      y += con.getDst().getYOffset(con);
    } else {
      assert( getOutEdges().contains(con) );
      x = -size.x / 2 + Interface.WIDTH;
      int index = input.indexOf(con.getSrc());
      assert (index >= 0);
      y = index*Y_IFACE_DIST+Y_WORLD_IFACE_OFFSET;
      y += con.getSrc().getYOffset(con);
    }
    return new Point( size.x/2+x, y);
  }

}
