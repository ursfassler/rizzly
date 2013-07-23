package pir.traverser;

import java.util.Collection;

import pir.DefTraverser;
import pir.type.NamedElement;
import pir.type.Type;

//TODO used?
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
