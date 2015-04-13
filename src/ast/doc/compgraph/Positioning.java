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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import metadata.parser.SimpleMetaParser;
import util.Pair;
import util.PointF;
import util.SimpleGraph;

import common.ElementInfo;

import error.ErrorType;
import error.RError;

public class Positioning {
  private static final double X_COMP_DIST = 140;
  private static final double Y_COMP_OFFSET = 20;
  private static final double Y_COMP_SPACE = 15;

  public static void doPositioning(WorldComp g) {
    Set<Connection> done = new HashSet<Connection>();
    List<Component> next = new ArrayList<Component>();

    for (Connection con : g.getOutEdges()) {
      if (!next.contains(con.getDst().getOwner())) {
        next.add(con.getDst().getOwner());
      }
      done.add(con);
    }

    List<List<SubComponent>> topolist = doToposort(g);

    double height = Math.max(g.getInput().size(), g.getOutput().size()) * Component.Y_IFACE_DIST + Component.Y_WORLD_IFACE_OFFSET;
    double xdist = X_COMP_DIST;

    for (int i = 0; i < topolist.size(); i++) {
      List<SubComponent> complist = topolist.get(i);

      double y = Y_COMP_OFFSET;
      for (SubComponent comp : complist) {
        PointF pos = comp.getPos();
        pos.y = y;
        pos.x = xdist * (i + 1);
        y = y + comp.getSize().y + Y_COMP_SPACE;
      }
      height = Math.max(height, y);
    }

    g.getSize().x = (topolist.size() + 1) * xdist;
    g.getSize().y = height;

    appendMeta(g);
    for (SubComponent sub : g.getComp()) {
      appendMeta(sub);
    }
    for (Connection con : g.getConn()) {
      appendMeta(con);
    }
  }

  private static void appendMeta(Connection con) {
    if (con.getMetadata().isEmpty()) {
      return;
    }
    Map<String, String> data = SimpleMetaParser.parse(con.getMetadata());

    PointF src = new PointF(con.getSrc().getOwner().getSrcPort(con));
    PointF dst = new PointF(con.getDst().getOwner().getDstPort(con));

    double[] path = getPath(data);
    assert (path.length % 2 == 1);
    boolean setX = true;
    PointF lastPos = src;
    for (double itr : path) {
      PointF nextPos;
      if (setX) {
        nextPos = new PointF(itr, lastPos.y);
      } else {
        nextPos = new PointF(lastPos.x, itr);
      }
      con.getVias().add(new Via(nextPos));
      lastPos = nextPos;
      setX = !setX;
    }
    con.getVias().add(new Via(new PointF(lastPos.x, dst.y)));
  }

  private static double[] getPath(Map<String, String> data) {
    assert (data.containsKey("path"));
    String paths = data.get("path");
    StringTokenizer st = new StringTokenizer(paths, " ");
    double path[] = new double[st.countTokens()];
    int i = 0;
    while (st.hasMoreTokens()) {
      path[i] = Double.parseDouble(st.nextToken());
      i++;
    }
    return path;
  }

  private static void appendMeta(SubComponent sub) {
    if (sub.getMetadata().isEmpty()) {
      return;
    }
    Map<String, String> data = SimpleMetaParser.parse(sub.getMetadata());

    sub.getPos().x = getDouble(data, "x", sub.getInfo());
    sub.getPos().y = getDouble(data, "y", sub.getInfo());
  }

  private static void appendMeta(WorldComp g) {
    if (g.getMetadata().isEmpty()) {
      return;
    }
    Map<String, String> data = SimpleMetaParser.parse(g.getMetadata());

    g.getSize().x = getDouble(data, "width", g.getInfo());
    g.getSize().y = getDouble(data, "height", g.getInfo());
  }

  private static double getDouble(Map<String, String> data, String key, ElementInfo info) {

    if (!data.containsKey(key)) {
      RError.err(ErrorType.Error, info, "expected metadata: " + key);
    }
    return Double.parseDouble(data.get(key));
  }

  private static List<List<SubComponent>> doToposort(WorldComp comp) {
    HashMap<Component, Integer> map = new HashMap<Component, Integer>();
    SimpleGraph<Integer> g = new SimpleGraph<Integer>();
    int nr = 0;

    for (Component u : comp.getComp()) {
      map.put(u, nr);
      g.addVertex(nr);
      nr++;
    }
    Set<Integer> start = new HashSet<Integer>(g.vertexSet());
    for (Connection e : comp.getConn()) {
      Integer u = map.get(e.getSrc().getOwner());
      Integer v = map.get(e.getDst().getOwner());
      if ((u != null) && (v != null)) { // otherwise one end is at the owner
                                        // component -> not relevant
        g.addEdge(u, v);
        start.remove(v);
      }
    }

    List<Set<Integer>> levels = new ArrayList<Set<Integer>>();

    while (!start.isEmpty()) {
      levels.add(start);

      Set<Integer> follow = new HashSet<Integer>();
      for (Integer v : start) {
        for (Pair<Integer, Integer> e : g.outgoingEdgesOf(v)) {
          follow.add(e.second);
          g.removeEdge(e);
        }
      }

      Set<Integer> next = new HashSet<Integer>();
      for (Integer v : follow) {
        if (g.inDegreeOf(v) == 0) {
          next.add(v);
        }
      }

      start = next;
    }

    List<List<SubComponent>> topolist = new ArrayList<List<SubComponent>>();
    for (Set<Integer> itr : levels) {
      LinkedList<Integer> sub = new LinkedList<Integer>(itr);
      Collections.sort(sub);
      List<SubComponent> ol = new ArrayList<SubComponent>();
      for (int v : sub) {
        ol.add(comp.getComp().get(v));
      }
      topolist.add(ol);
    }

    return topolist;
  }

}
