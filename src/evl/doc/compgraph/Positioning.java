package evl.doc.compgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import util.Pair;
import util.Point;
import util.SimpleGraph;

public class Positioning {
  private static final int X_COMP_DIST = 140;
  private static final int Y_COMP_OFFSET = 20;
  private static final int Y_COMP_SPACE = 15;

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

    int height = Math.max(g.getInput().size(), g.getOutput().size()) * Component.Y_IFACE_DIST + Component.Y_WORLD_IFACE_OFFSET;
    int xdist = X_COMP_DIST;

    for (int i = 0; i < topolist.size(); i++) {
      List<SubComponent> complist = topolist.get(i);

      int y = Y_COMP_OFFSET;
      for (SubComponent comp : complist) {
        Point pos = comp.getPos();
        pos.y = y;
        pos.x = xdist * (i + 1);
        y = y + comp.getSize().y + Y_COMP_SPACE;
      }
      height = Math.max(height, y);
    }

    g.getSize().x = (topolist.size() + 1) * xdist;
    g.getSize().y = height;
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
      if ((u != null) && (v != null)) { // otherwise one end is at the owner component -> not relevant
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
