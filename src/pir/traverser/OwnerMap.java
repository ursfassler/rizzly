package pir.traverser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import pir.DefTraverser;
import pir.PirObject;
import pir.statement.Statement;

public class OwnerMap extends DefTraverser<Void, Statement> {
  private MapMaker mapMaker = new MapMaker();
  private HashMap<PirObject, Statement> owner = new HashMap<PirObject, Statement>();

  public static HashMap<PirObject, Statement> make(PirObject obj) {
    OwnerMap maker = new OwnerMap();
    maker.traverse(obj, null);
    return maker.owner;
  }

  @Override
  protected Void visitStatement(Statement obj, Statement param) {
    Set<PirObject> set = new HashSet<PirObject>();
    mapMaker.traverse(obj, set);
    for (PirObject itr : set) {
      assert (!owner.containsKey(itr));
      owner.put(itr, obj);
    }
    return null;
  }

}

class MapMaker extends DefTraverser<Void, Set<PirObject>> {

  @Override
  protected Void visit(PirObject obj, Set<PirObject> param) {
    param.add(obj);
    super.visit(obj, param);
    return null;
  }

}
