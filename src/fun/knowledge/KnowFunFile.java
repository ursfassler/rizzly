package fun.knowledge;

import java.util.HashMap;
import java.util.Map;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.other.Named;
import fun.other.RizzlyFile;

/**
 * Knows in which file an object is.
 * 
 * @author urs
 * 
 */
public class KnowFunFile extends KnowledgeEntry {
  private KnowledgeBase base;
  private Map<Named, RizzlyFile> cache = new HashMap<Named, RizzlyFile>();

  @Override
  public void init(KnowledgeBase base) {
    this.base = base;
  }

  public RizzlyFile get(Named obj) {
    RizzlyFile ret = find(obj);
    if (ret == null) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Object not reachable: " + obj);
    }
    return ret;
  }

  public RizzlyFile find(Named obj) {
    RizzlyFile ret = cache.get(obj);
    if (ret == null) {
      rebuild();
      ret = cache.get(obj);
    }
    return ret;
  }

  private void rebuild() {
    cache.clear();
    KnowFileTraverser traverser = new KnowFileTraverser(cache);
    for (RizzlyFile file : base.getFiles()) {
      traverser.traverse(file, null);
    }
  }

  public void clear() {
    cache.clear();
  }

}

class KnowFileTraverser extends DefTraverser<Void, RizzlyFile> {
  private Map<Named, RizzlyFile> cache;

  public KnowFileTraverser(Map<Named, RizzlyFile> cache) {
    super();
    this.cache = cache;
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, RizzlyFile param) {
    assert (param == null);
    return super.visitRizzlyFile(obj, obj);
  }

  @Override
  protected Void visit(Fun obj, RizzlyFile param) {
    if (obj instanceof Named) {
      if (cache.containsKey(obj)) {
        RizzlyFile oldparent = cache.get(obj);
        RError.err(ErrorType.Fatal, obj.getInfo(), "Same object (" + obj + ") found 2 times: " + oldparent.getFullName() + " and " + param.getFullName());
      }
      cache.put((Named) obj, param);
    }
    return super.visit(obj, param);
  }

}
