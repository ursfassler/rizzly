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

public class Interface {
  final static public double WIDTH = 46;
  final static public double HEIGHT = 10;
  final private Component owner;
  final private String instname;
  final private List<Connection> connection = new ArrayList<Connection>();

  public Interface(Component owner, String instname) {
    super();
    this.owner = owner;
    this.instname = instname;
  }

  public List<Connection> getConnection() {
    return connection;
  }

  public Component getOwner() {
    return owner;
  }

  public String getInstname() {
    return instname;
  }

  @Override
  public String toString() {
    return owner.toString() + "." + instname;
  }

  public double getYOffset(Connection con) {
    int idx = connection.indexOf(con);
    if (idx >= 0) {
      double h = HEIGHT * (idx + 1) / (connection.size() + 1);
      return h - HEIGHT / 2;
    } else {
      return Double.NaN;
    }
  }

}
