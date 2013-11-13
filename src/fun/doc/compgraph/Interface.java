package fun.doc.compgraph;

import java.util.ArrayList;
import java.util.List;

public class Interface {
  final static public int WIDTH = 46;
  final static public int HEIGHT = 10;
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

  public int getYOffset(Connection con) {
    int idx = connection.indexOf(con);
    if (idx >= 0) {
      int h = HEIGHT * (idx + 1) / (connection.size() + 1);
      return h - HEIGHT / 2;
    } else {
      return -1;
    }
  }

}
