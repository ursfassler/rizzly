package pir.traverser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import pir.DefTraverser;
import pir.PirObject;
import pir.other.SsaVariable;
import pir.statement.Statement;

public class OwnerMap extends DefTraverser<Void, PirObject> {
  private MapMaker mapMaker = new MapMaker();
  private HashMap<SsaVariable, Statement> owner = new HashMap<SsaVariable, Statement>();

  public static HashMap<SsaVariable, Statement> make(PirObject obj) {
    OwnerMap maker = new OwnerMap();
    maker.traverse(obj, null);
    return maker.owner;
  }

  @Override
  protected Void visitStatement(Statement obj, PirObject param) {
    assert (param == null);
    Set<SsaVariable> set = new HashSet<SsaVariable>();
    mapMaker.traverse(obj, set);
    for (SsaVariable itr : set) {
      assert (!owner.containsKey(itr));
      owner.put(itr, obj);
    }
    return null;
  }

}

class MapMaker extends DefTraverser<Void, Set<SsaVariable>> {

  @Override
  protected Void visitSsaVariable(SsaVariable obj, Set<SsaVariable> param) {
    param.add(obj);
    super.visitSsaVariable(obj, param);
    return null;
  }

}
