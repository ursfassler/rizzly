package fun.traverser;

import java.util.Map;

import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.ReferenceLinked;
import fun.other.Named;

public class ReLinker extends DefTraverser<Void, Map<? extends Named, ? extends Named>> {

  public static void process(Fun classes, Map<? extends Named, ? extends Named> map) {
    ReLinker reLinker = new ReLinker();
    reLinker.traverse(classes, map);
  }

  @Override
  protected Void visitReferenceLinked(ReferenceLinked obj, Map<? extends Named, ? extends Named> param) {
    Named target = param.get(obj.getLink());
    if (target != null) {
      obj.setLink(target);
    }
    return super.visitReferenceLinked(obj, param);
  }

}
