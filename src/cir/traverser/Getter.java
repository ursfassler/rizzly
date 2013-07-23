package cir.traverser;

import java.util.ArrayList;
import java.util.List;

import cir.CirBase;
import cir.DefTraverser;


public class Getter<R, T> extends DefTraverser<Void, T> {
  private List<R> list = new ArrayList<R>();

  public List<R> get(CirBase ast, T param) {
    traverse(ast, param);
    return list;
  }

  protected Void add(R obj) {
    list.add(obj);
    return null;
  }
}
