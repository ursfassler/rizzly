package cir.knowledge;

import cir.CirBase;
import cir.NullTraverser;
import cir.expression.reference.RefCall;
import cir.expression.reference.RefIndex;
import cir.expression.reference.RefItem;
import cir.expression.reference.RefName;
import cir.expression.reference.Reference;
import cir.other.Variable;
import cir.type.ArrayType;
import cir.type.EnumElement;
import cir.type.NamedElemType;
import cir.type.NamedElement;
import cir.type.Type;
import cir.type.TypeRef;

public class KnowType extends KnowledgeEntry {
  private static KnowTypeTraverser traverser = new KnowTypeTraverser();

  @Override
  public void init(KnowledgeBase base) {
    throw new RuntimeException("not yet implemented");
  }

  public static Type get(Reference ref) {
    Type t = traverser.traverse(ref.getRef(), null);
    for (RefItem itm : ref.getOffset()) {
      t = traverser.traverse(itm, t);
    }
    return t;
  }

  public static boolean isComposition(Type type) {
    return (type instanceof NamedElemType) || (type instanceof ArrayType);
  }

}

class KnowTypeTraverser extends NullTraverser<Type, Type> {

  @Override
  protected Type visitDefault(CirBase obj, Type param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitRefCall(RefCall obj, Type param) {
    // Type pt = visit(obj.getPrevious(), param);
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitRefName(RefName obj, Type param) {
    CirBase sub = KnowChild.find(param, obj.getName());
    assert (sub != null);
    return visit(sub, param);
  }

  @Override
  protected Type visitRefIndex(RefIndex obj, Type param) {
    ArrayType array = (ArrayType) param;
    return visit(array.getType(), null);
  }

  @Override
  protected Type visitVariable(Variable obj, Type param) {
    return visit(obj.getType(), param);
  }

  @Override
  protected Type visitEnumElement(EnumElement obj, Type param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitNamedElement(NamedElement obj, Type param) {
    return visit(obj.getType(), param);
  }

  @Override
  protected Type visitTypeRef(TypeRef obj, Type param) {
    return obj.getRef();
  }

}
