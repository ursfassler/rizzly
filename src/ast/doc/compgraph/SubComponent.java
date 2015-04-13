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
import java.util.List;

import util.PointF;

import common.Designator;
import common.ElementInfo;
import common.Metadata;

final public class SubComponent extends Component {
  private static final PointF ErrorPoint = new PointF(Double.NaN, Double.NaN);
  final private String instname;
  final private PointF pos = new PointF();

  public SubComponent(ElementInfo info, String instname, Designator path, String classname, List<Metadata> metadata) {
    super(info, path, classname, metadata);
    this.instname = instname;
  }

  public String getInstname() {
    return instname;
  }

  @Override
  public PointF getSize() {
    PointF size = new PointF();

    size.x = SUBCOMP_WIDTH;
    size.y = Math.max(input.size(), output.size()) * Y_IFACE_DIST + Y_SUBC_IFACE_OFFSET;

    return size;
  }

  @Override
  public ArrayList<Connection> getInEdges() {
    ArrayList<Connection> ret = new ArrayList<Connection>();
    for (Interface iface : input) {
      ret.addAll(iface.getConnection());
    }
    return ret;
  }

  @Override
  public ArrayList<Connection> getOutEdges() {
    ArrayList<Connection> ret = new ArrayList<Connection>();
    for (Interface iface : output) {
      ret.addAll(iface.getConnection());
    }
    return ret;
  }

  @Override
  public PointF getPos() {
    return pos;
  }

  @Override
  public String toString() {
    return instname;
  }

  @Override
  public PointF getSrcPort(Connection con) {
    if (!getOutEdges().contains(con)) {
      return ErrorPoint;
    }
    double x;
    int index;
    x = SUBCOMP_WIDTH / 2;
    index = output.indexOf(con.getSrc());
    if (index < 0) {
      return ErrorPoint;
    }
    double y = pos.y + index * Y_IFACE_DIST + Y_SUBC_IFACE_OFFSET;
    double yOffset = con.getSrc().getYOffset(con);
    y += yOffset;
    return new PointF(pos.x + x, y);
  }

  @Override
  public PointF getDstPort(Connection con) {
    if (!getInEdges().contains(con)) {
      return ErrorPoint;
    }
    double x;
    int index;
    x = -SUBCOMP_WIDTH / 2;
    index = input.indexOf(con.getDst());
    if (index < 0) {
      return ErrorPoint;
    }
    double y = pos.y + index * Y_IFACE_DIST + Y_SUBC_IFACE_OFFSET;
    double yOffset = con.getDst().getYOffset(con);
    y += yOffset;
    return new PointF(pos.x + x, y);
  }
}
