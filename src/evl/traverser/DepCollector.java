package evl.traverser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.BaseRef;

public class DepCollector extends DefTraverser<Void, Void> {

  private Set<Evl> visited = new HashSet<Evl>();

  public static Set<Evl> process(Evl top) {
    DepCollector collector = new DepCollector();
    collector.traverse(top, null);
    return collector.visited;
  }

  public static Set<Evl> process(Collection<? extends Evl> pubfunc) {
    DepCollector collector = new DepCollector();
    for (Evl func : pubfunc) {
      collector.traverse(func, null);
    }
    return collector.visited;
  }

  @Override
  protected Void visit(Evl obj, Void param) {
    if (!visited.contains(obj)) {
      visited.add(obj);
      super.visit(obj, param);
    }
    return null;
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Void param) {
    super.visitBaseRef(obj, param);
    visit(obj.getLink(), param);
    return null;
  }

}
