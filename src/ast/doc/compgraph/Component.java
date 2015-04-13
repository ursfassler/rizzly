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

public abstract class Component implements Vertex {
  public static final double Y_IFACE_DIST = 15;
  public static final double Y_SUBC_IFACE_OFFSET = 35;
  public static final double Y_WORLD_IFACE_OFFSET = 55;
  public static final double SUBCOMP_WIDTH = 100;

  final protected Designator path;
  final protected String classname;
  final protected List<Metadata> metadata;
  final protected ArrayList<Interface> input = new ArrayList<Interface>();
  final protected ArrayList<Interface> output = new ArrayList<Interface>();

  final private ElementInfo info;

  public Component(ElementInfo info, Designator path, String classname, List<Metadata> metadata) {
    super();
    this.path = path;
    this.classname = classname;
    this.metadata = metadata;
    this.info = info;
  }

  public ElementInfo getInfo() {
    return info;
  }

  public List<Metadata> getMetadata() {
    return metadata;
  }

  public abstract PointF getSize();

  public String getClassname() {
    return classname;
  }

  public Designator getPath() {
    return path;
  }

  public ArrayList<Interface> getInput() {
    return input;
  }

  public ArrayList<Interface> getOutput() {
    return output;
  }

  public abstract ArrayList<Connection> getInEdges();

  public abstract ArrayList<Connection> getOutEdges();

  public abstract PointF getSrcPort(Connection con);

  public abstract PointF getDstPort(Connection con);

}
