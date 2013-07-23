package cir.knowledge;

import cir.CirBase;
import cir.NullTraverser;
import cir.expression.reference.RefCall;
import cir.expression.reference.RefHead;
import cir.expression.reference.RefIndex;
import cir.expression.reference.RefItem;
import cir.expression.reference.RefName;
import cir.other.Variable;
import cir.type.ArrayType;
import cir.type.EnumElement;
import cir.type.NamedElement;
import cir.type.Type;

public class KnowType extends KnowledgeEntry {
  private static KnowTypeTraverser traverser = new KnowTypeTraverser();

  @Override
  public void init(KnowledgeBase base) {
    throw new RuntimeException("not yet implemented");
  }

  public static Type get(RefItem obj) {
    return traverser.traverse(obj, null);
  }

}

class KnowTypeTraverser extends NullTraverser<Type, Void> {

  @Override
  protected Type visitDefault(CirBase obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitRefCall(RefCall obj, Void param) {
    // Type pt = visit(obj.getPrevious(), param);
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitRefName(RefName obj, Void param) {
    Type pt = visit(obj.getPrevious(), param);
    CirBase sub = KnowChild.find(pt, obj.getName());
    assert (sub != null);
    return visit(sub, param);
  }

  @Override
  protected Type visitRefIndex(RefIndex obj, Void param) {
    Type pt = visit(obj.getPrevious(), param);
    ArrayType array = (ArrayType) pt;
    return array.getType();
  }

  @Override
  protected Type visitRefHead(RefHead obj, Void param) {
    return visit((CirBase) obj.getRef(), param);
  }

  @Override
  protected Type visitVariable(Variable obj, Void param) {
    return obj.getType();
  }

  @Override
  protected Type visitEnumElement(EnumElement obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitNamedElement(NamedElement obj, Void param) {
    return obj.getType();
  }

}
