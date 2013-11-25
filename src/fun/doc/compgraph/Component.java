package fun.doc.compgraph;

import java.util.ArrayList;
import java.util.List;

import util.PointF;

import common.Designator;
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

  public Component(Designator path, String classname, List<Metadata> metadata) {
    super();
    this.path = path;
    this.classname = classname;
    this.metadata = metadata;
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
