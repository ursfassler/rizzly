/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package ast.doc.compgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.PointF;

import common.Designator;
import common.ElementInfo;
import common.Metadata;

public class WorldComp extends Component {
  final private PointF size = new PointF(140, 35);

  final private List<SubComponent> comp = new ArrayList<SubComponent>();
  final private Set<Connection> conn = new HashSet<Connection>();

  public WorldComp(ElementInfo info, Designator path, String classname, List<Metadata> metadata) {
    super(info, path, classname, metadata);
  }

  public List<SubComponent> getComp() {
    return comp;
  }

  public Set<Connection> getConn() {
    return conn;
  }

  @Override
  public Designator getPath() {
    return path;
  }

  @Override
  public PointF getSize() {
    return size;
  }

  @Override
  public ArrayList<Connection> getInEdges() {
    ArrayList<Connection> ret = new ArrayList<Connection>();
    for (Interface iface : output) {
      ret.addAll(iface.getConnection());
    }
    return ret;
  }

  @Override
  public ArrayList<Connection> getOutEdges() {
    ArrayList<Connection> ret = new ArrayList<Connection>();
    for (Interface iface : input) {
      ret.addAll(iface.getConnection());
    }
    return ret;
  }

  @Override
  public PointF getPos() {
    PointF pos = new PointF();
    pos.x = size.x / 2;
    pos.y = 0;
    return pos;
  }

  @Override
  public String toString() {
    return getClassname();
  }

  @Override
  public PointF getSrcPort(Connection con) {
    assert (getOutEdges().contains(con));
    double x;
    double y;
    x = -size.x / 2 + Interface.WIDTH;
    int index = input.indexOf(con.getSrc());
    assert (index >= 0);
    y = index * Y_IFACE_DIST + Y_WORLD_IFACE_OFFSET;
    y += con.getSrc().getYOffset(con);
    return new PointF(size.x / 2 + x, y);
  }

  @Override
  public PointF getDstPort(Connection con) {
    assert (getInEdges().contains(con));
    double x;
    double y;
    x = size.x / 2 - Interface.WIDTH;
    int index = output.indexOf(con.getDst());
    assert (index >= 0);
    y = index * Y_IFACE_DIST + Y_WORLD_IFACE_OFFSET;
    y += con.getDst().getYOffset(con);
    return new PointF(size.x / 2 + x, y);
  }
}
