package evl.knowledge;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import common.Designator;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.type.base.EnumElement;

/**
 * Knows the parent of most objects
 */
public class KnowParent extends KnowledgeEntry {
  private KnowledgeBase base;
  private Map<Evl, Evl> cache = new HashMap<Evl, Evl>();

  @Override
  public void init(KnowledgeBase base) {
    this.base = base;
  }

  public Evl getParent(Evl obj) {
    Evl ret = cache.get(obj);
    if (ret == null) {
      rebuild();
      ret = cache.get(obj);
    }
    if (ret == null) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Object not reachable: " + obj);
    }
    return ret;
  }

  private void rebuild() {
    cache.clear();
    KnowParentTraverser traverser = new KnowParentTraverser(cache);
    traverser.traverse(base.getRoot(), null);
  }

}

class KnowParentTraverser extends DefTraverser<Void, Evl> {
  private Map<Evl, Evl> cache;

  public KnowParentTraverser(Map<Evl, Evl> cache) {
    super();
    this.cache = cache;
  }

  private Designator getPath(Evl obj) {
    LinkedList<String> name = new LinkedList<String>();
    while (obj != null) {
      name.push(obj.toString());
      obj = cache.get(obj);
    }
    return new Designator(name);
  }

  @Override
  protected Void visit(Evl obj, Evl param) {
    assert (obj != param);
    if (cache.containsKey(obj)) {
      if (!(obj instanceof EnumElement)) { // FIXME remove this hack (new enum type system?)
        Evl oldparent = cache.get(obj);
        RError.err(ErrorType.Hint, "First time was here:  " + getPath(oldparent));
        RError.err(ErrorType.Hint, "Second time was here: " + getPath(param));
        RError.err(ErrorType.Fatal, obj.getInfo(), "Same object (" + obj + ") found 2 times");
      }
    }
    cache.put(obj, param);
    return super.visit(obj, obj);
  }

}
