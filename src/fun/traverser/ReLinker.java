package fun.traverser;

import java.util.Map;

import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.BaseRef;
import fun.other.Named;

public class ReLinker extends DefTraverser<Void, Map<Fun, Fun>> {

  public static void process(Fun classes, Map<Fun, Fun> map) {
    ReLinker reLinker = new ReLinker();
    reLinker.traverse(classes, map);
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Map<Fun, Fun> param) {
    Fun target = param.get(obj.getLink());
    if (target != null) {
      obj.setLink((Named) target);
    }
    return super.visitBaseRef(obj, param);
  }

}
