package pir.traverser;

import pir.DefTraverser;
import pir.type.Type;
import pir.type.TypeRef;

public abstract class TypeReplacer<T> extends DefTraverser<Type, T> {

  @Override
  protected Type visitTypeRef(TypeRef obj, T param) {
    obj.setRef(visit(obj.getRef(),param));
    return null;
  }

}
