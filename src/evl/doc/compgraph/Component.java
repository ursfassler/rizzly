package evl.doc.compgraph;

import java.util.ArrayList;

import util.Point;

import common.Designator;

public abstract class Component implements Vertex {
  public static final int Y_IFACE_DIST = 25;
  public static final int Y_SUBC_IFACE_OFFSET = 35;
  public static final int Y_WORLD_IFACE_OFFSET = 55;
  public static final int SUBCOMP_WIDTH = 100;

  final protected Designator path;
  final protected String classname;
  final protected ArrayList<Interface> input = new ArrayList<Interface>();
  final protected ArrayList<Interface> output = new ArrayList<Interface>();

  public Component(Designator path, String classname) {
    super();
    this.path = path;
    this.classname = classname;
  }

  public abstract Point getSize();

  public String getClassname() {
    return classname;
  }

  public Designator getFullName() {
    return new Designator(path, classname);
  }

  public ArrayList<Interface> getInput() {
    return input;
  }

  public ArrayList<Interface> getOutput() {
    return output;
  }

  public abstract ArrayList<Connection> getInEdges();

  public abstract ArrayList<Connection> getOutEdges();

  public abstract Point getPort(Connection con);

}
