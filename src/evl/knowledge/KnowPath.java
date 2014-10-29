package evl.knowledge;

import java.util.HashMap;
import java.util.Map;

import common.Designator;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.other.Named;

public class KnowPath extends KnowledgeEntry {
  private KnowledgeBase base;
  private Map<Evl, Designator> cache = new HashMap<Evl, Designator>();

  @Override
  public void init(KnowledgeBase base) {
    this.base = base;
  }

  public Designator get(Evl obj) {
    Designator ret = find(obj);
    if (ret == null) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Object not reachable: " + obj);
    }
    return ret;
  }

  public Designator find(Evl obj) {
    Designator ret = cache.get(obj);
    if (ret == null) {
      rebuild();
      ret = cache.get(obj);
    }
    return ret;
  }

  private void rebuild() {
    cache.clear();
    KnowPathTraverser traverser = new KnowPathTraverser(cache, base);
    traverser.traverse(base.getRoot(), new Designator());
  }

  public void clear() {
    cache.clear();
  }

}

class KnowPathTraverser extends DefTraverser<Void, Designator> {
  final private Map<Evl, Designator> cache;

  public KnowPathTraverser(Map<Evl, Designator> cache, KnowledgeBase kb) {
    super();
    this.cache = cache;
  }

  @Override
  protected Void visit(Evl obj, Designator param) {
    if (cache.containsKey(obj)) {
      Designator oldparent = cache.get(obj);
      RError.err(ErrorType.Fatal, obj.getInfo(), "Same object (" + obj + ") found 2 times: " + oldparent + " and " + param);
    }
    cache.put(obj, param);
    if (obj instanceof Named) {
      super.visit(obj, new Designator(param, ((Named) obj).getName()));
    }
    return null;
  }

}
