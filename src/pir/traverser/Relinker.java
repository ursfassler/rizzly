package pir.traverser;

import java.util.Map;

import pir.DefTraverser;
import pir.Pir;
import pir.expression.reference.Referencable;
import pir.expression.reference.Reference;

public class Relinker extends DefTraverser<Void, Map<? extends Referencable, ? extends Referencable>> {

  public static void process(Pir obj, Map<? extends Referencable, ? extends Referencable> map) {
    Relinker relinker = new Relinker();
    relinker.traverse(obj, map);
  }

  @Override
  protected Void visit(Pir obj, Map<? extends Referencable, ? extends Referencable> param) {
    if (obj instanceof Reference) {
      Reference ref = (Reference) obj;
      Referencable ntarget = param.get(ref.getRef());
      if (ntarget != null) {
        ref.setRef(ntarget);
      }
    }
    return super.visit(obj, param);
  }

}
