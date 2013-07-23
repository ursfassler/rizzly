package fun.traverser;

import java.util.ArrayList;
import java.util.List;

import fun.DefGTraverser;
import fun.Fun;

public class Getter<R, T> extends DefGTraverser<Void, T> {
  private List<R> list = new ArrayList<R>();

  public List<R> get(Fun ast, T param) {
    traverse(ast, param);
    return list;
  }

  protected Void add(R obj) {
    list.add(obj);
    return null;
  }
}
