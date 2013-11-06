package cir.traverser;

import java.util.Map;

import cir.Cir;
import cir.DefTraverser;
import cir.expression.reference.Referencable;
import cir.expression.reference.Reference;
import cir.type.Type;
import cir.type.TypeRef;

public class Relinker extends DefTraverser<Void, Map<? extends Referencable, ? extends Referencable>> {

  public static void process(Cir obj, Map<? extends Referencable, ? extends Referencable> map) {
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
    obj.setRef((Type) replace(obj.getRef(), param));
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Map<? extends Referencable, ? extends Referencable> param) {
    super.visitReference(obj, param);
    obj.setRef(replace(obj.getRef(), param));
    return null;
  }

}
