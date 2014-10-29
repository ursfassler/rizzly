package evl.copy;

import java.util.Map;

import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.BaseRef;
import evl.other.EvlList;
import evl.other.Named;

public class Relinker extends DefTraverser<Void, Map<? extends Named, ? extends Named>> {
  static private final Relinker INSTANCE = new Relinker();

  static public void relink(Evl obj, Map<? extends Named, ? extends Named> map) {
    INSTANCE.traverse(obj, map);
  }

  static public void relink(EvlList<? extends Evl> obj, Map<? extends Named, ? extends Named> map) {
    for (Evl itr : obj) {
      relink(itr, map);
    }
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Map<? extends Named, ? extends Named> param) {
    if (param.containsKey(obj.getLink())) {
      obj.setLink(param.get(obj.getLink()));
    }
    return super.visitBaseRef(obj, param);
  }
}
