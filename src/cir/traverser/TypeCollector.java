package cir.traverser;

import java.util.Collection;

import cir.DefTraverser;
import cir.type.NamedElement;
import cir.type.Type;

public class TypeCollector extends DefTraverser<Void, Collection<Type>> {

  @Override
  protected Void visitType(Type obj, Collection<Type> param) {
    if (param.contains(obj)) {
      return null;
    } else {
      param.add(obj);
      return super.visitType(obj, param);
    }
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, Collection<Type> param) {
    visit(obj.getType(), param);
    return null;
  }

}
