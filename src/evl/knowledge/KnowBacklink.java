package evl.knowledge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.BaseRef;
import evl.other.Named;

public class KnowBacklink extends KnowledgeEntry {
  private KnowledgeBase kb;
  final private Map<Evl, Set<BaseRef<Named>>> cache = new HashMap<Evl, Set<BaseRef<Named>>>();

  @Override
  public void init(KnowledgeBase base) {
    kb = base;
  }

  public Set<BaseRef<Named>> get(Evl target) {
    Set<BaseRef<Named>> set = cache.get(target);
    if (set == null) {
      cache.clear();
      BacklinkTraverser traverser = new BacklinkTraverser();
      traverser.traverse(kb.getRoot(), cache);
      set = new HashSet<BaseRef<Named>>();
      if (set == null) {
        RError.err(ErrorType.Fatal, target.getInfo(), "Object not reachable:" + target);
      }
    }
    return set;
  }

}

class BacklinkTraverser extends DefTraverser<Void, Map<Evl, Set<BaseRef<Named>>>> {

  @Override
  protected Void visit(Evl obj, Map<Evl, Set<BaseRef<Named>>> param) {
    if (!param.containsKey(obj)) {
      param.put(obj, new HashSet<BaseRef<Named>>());
    }
    return super.visit(obj, param);
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Map<Evl, Set<BaseRef<Named>>> param) {
    Set<BaseRef<Named>> set = param.get(obj.getLink());
    if (set == null) {
      set = new HashSet<BaseRef<Named>>();
      param.put(obj.getLink(), set);
    }
    set.add(obj);
    return super.visitBaseRef(obj, param);
  }

}
