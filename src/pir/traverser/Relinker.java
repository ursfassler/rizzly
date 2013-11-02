package pir.traverser;

import java.util.Map;

import pir.DefTraverser;
import pir.Pir;
import pir.expression.reference.Referencable;
import pir.expression.reference.Reference;
import pir.type.Type;
import pir.type.TypeRef;

public class Relinker extends DefTraverser<Void, Map<? extends Referencable, ? extends Referencable>> {

  public static void process(Pir obj, Map<? extends Referencable, ? extends Referencable> map) {
    Relinker relinker = new Relinker();
    relinker.traverse(obj, map);
  }

  private Referencable replace(Referencable ref, Map<? extends Referencable, ? extends Referencable> param) {
    Referencable ntarget = param.get(ref);
    if (ntarget != null) {
      return ntarget;
    } else {
      return ref;
    }
  }

  @Override
  protected Void visitTypeRef(TypeRef obj, Map<? extends Referencable, ? extends Referencable> param) {
    super.visitTypeRef(obj, param);
    obj.setRef((Type) replace(obj.getRef(),param));
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Map<? extends Referencable, ? extends Referencable> param) {
    super.visitReference(obj, param);
    obj.setRef(replace(obj.getRef(),param));
    return null;
  }

}
