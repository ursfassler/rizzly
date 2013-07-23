package fun.doc.compgraph;

import java.util.ArrayList;
import java.util.List;

import common.Designator;



public class Interface {
  final static public int WIDTH = 46;
  final static public int HEIGHT = 20;
  final private Component owner;
  final private String instname;
  final private Designator path;
  final private String classname;
  final private List<Connection> connection = new ArrayList<Connection>();

  public Interface(Component owner, String instname, Designator path, String classname) {
    super();
    this.owner = owner;
    this.instname = instname;
    this.path = path;
    this.classname = classname;
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

  public String getClassname() {
    return classname;
  }

  @Override
  public String toString() {
    return owner.toString() + "." + instname;
  }

  public int getYOffset(Connection con) {
    int idx = connection.indexOf(con);
    assert (idx >= 0);
    int h = HEIGHT * (idx + 1) / (connection.size() + 1);
    return h - HEIGHT / 2;
  }

  public Designator getFullName() {
    return new Designator(path, classname);
  }

}
