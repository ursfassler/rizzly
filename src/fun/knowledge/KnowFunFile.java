package fun.knowledge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.Pair;

import common.Designator;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.other.RizzlyFile;

/**
 * Knows in which file an object is.
 * 
 * @author urs
 * 
 */
public class KnowFunFile extends KnowledgeEntry {
  private KnowledgeBase base;
  private Map<Fun, RizzlyFile> cache = new HashMap<Fun, RizzlyFile>();
  private Map<RizzlyFile, Designator> path = new HashMap<RizzlyFile, Designator>();

  @Override
  public void init(KnowledgeBase base) {
    this.base = base;
  }

  public RizzlyFile get(Fun obj) {
    RizzlyFile ret = find(obj);
    if (ret == null) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Object not reachable: " + obj);
    }
    return ret;
  }

  public RizzlyFile find(Fun obj) {
    RizzlyFile ret = cache.get(obj);
    if (ret == null) {
      rebuild();
      ret = cache.get(obj);
    }
    return ret;
  }

  public Designator path(RizzlyFile file) {
    return path.get(file);
  }

  private void rebuild() {
    clear();
    KnowFileTraverser traverser = new KnowFileTraverser(cache);
    Set<Pair<Designator, RizzlyFile>> items = new HashSet<Pair<Designator, RizzlyFile>>();
    base.getFiles().getItems(RizzlyFile.class, new Designator(), items);
    for (Pair<Designator, RizzlyFile> file : items) {
      traverser.traverse(file.second, null);
      path.put(file.second, file.first);
    }
  }

  public void clear() {
    cache.clear();
    path.clear();
  }

}

class KnowFileTraverser extends DefTraverser<Void, RizzlyFile> {
  private Map<Fun, RizzlyFile> cache;

  public KnowFileTraverser(Map<Fun, RizzlyFile> cache) {
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
    if (cache.containsKey(obj)) {
      RizzlyFile oldparent = cache.get(obj);
      RError.err(ErrorType.Fatal, obj.getInfo(), "Same object (" + obj + ") found 2 times: " + oldparent + " and " + param);
    }
    cache.put(obj, param);
    return super.visit(obj, param);
  }

}
