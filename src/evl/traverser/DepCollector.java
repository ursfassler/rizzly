package evl.traverser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.Reference;
import evl.other.CompUse;
import evl.other.Named;
import evl.type.TypeRef;
import evl.type.base.EnumDefRef;

public class DepCollector extends DefTraverser<Void, Void> {

  private Set<Named> visited = new HashSet<Named>();

  public static Set<Named> process(Evl top) {
    DepCollector collector = new DepCollector();
    collector.traverse(top, null);
    return collector.visited;
  }

  public static Set<Named> process(Collection<? extends Evl> pubfunc) {
    DepCollector collector = new DepCollector();
    for (Evl func : pubfunc) {
      collector.traverse(func, null);
    }
    return collector.visited;
  }

  @Override
  protected Void visit(Evl obj, Void param) {
    if (!visited.contains(obj)) {
      if (obj instanceof Named) {
        visited.add((Named) obj);
      }
      super.visit(obj, param);
    }
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    super.visitReference(obj, param);
    visit(obj.getLink(), param);
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, Void param) {
    super.visitCompUse(obj, param);
    visit(obj.getLink(), param);
    return null;
  }

  @Override
  protected Void visitTypeRef(TypeRef obj, Void param) {
    super.visitTypeRef(obj, param);
    visit(obj.getRef(), param);
    return null;
  }

  @Override
  protected Void visitEnumDefRef(EnumDefRef obj, Void param) {
    super.visitEnumDefRef(obj, param);
    visit(obj.getElem(), param);
    return null;
  }

}
