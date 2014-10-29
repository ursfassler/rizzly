package fun.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.other.CompImpl;
import fun.type.Type;

public class KnowImplementation extends KnowledgeEntry {
  private KnowledgeBase base;
  private final Map<Fun, List<Fun>> cache = new HashMap<Fun, List<Fun>>();

  @Override
  public void init(KnowledgeBase base) {
    this.base = base;
  }

  public List<Fun> get(Fun obj) {
    List<Fun> ret = find(obj);
    if (ret == null) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Object not reachable: " + obj);
    }
    return ret;
  }

  public List<Fun> find(Fun obj) {
    List<Fun> ret = cache.get(obj);
    if (ret == null) {
      rebuild();
      ret = cache.get(obj);
    }
    return ret;
  }

  private void rebuild() {
    cache.clear();
    KnowImplTraverser traverser = new KnowImplTraverser(cache);
    traverser.traverse(base.getRoot(), null);
  }

  public void clear() {
    cache.clear();
  }

}

class KnowImplTraverser extends DefTraverser<Void, Void> {

  private Map<Fun, List<Fun>> cache;

  public KnowImplTraverser(Map<Fun, List<Fun>> cache) {
    super();
    this.cache = cache;
  }

  private void add(Fun iface, Fun impl) {
    List<Fun> list = cache.get(iface);
    if (list == null) {
      list = new ArrayList<Fun>();
      cache.put(iface, list);
    }
    assert (!list.contains(impl));
    list.add(impl);
  }

  @Override
  protected Void visitComponent(CompImpl obj, Void param) {
    add(obj, obj);
    return super.visitComponent(obj, param);
  }

  @Override
  protected Void visitType(Type obj, Void param) {
    // TODO reimplement
    throw new RuntimeException("reimplement");
    // if (obj instanceof TypeGenerator) {
    // add(obj, obj);
    // }
    // return super.visitType(obj, param);
  }

}
