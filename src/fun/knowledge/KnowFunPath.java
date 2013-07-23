package fun.knowledge;

import java.util.HashMap;
import java.util.Map;

import common.Designator;

import error.ErrorType;
import error.RError;
import fun.DefGTraverser;
import fun.Fun;
import fun.function.FunctionHeader;
import fun.other.Named;
import fun.type.Type;

public class KnowFunPath extends KnowledgeEntry {
  private KnowledgeBase base;
  private Map<Named, Designator> cache = new HashMap<Named, Designator>();

  @Override
  public void init(KnowledgeBase base) {
    this.base = base;
  }

  public Designator get(Named obj) {
    Designator ret = find(obj);
    if (ret == null) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Object not reachable: " + obj);
    }
    return ret;
  }

  public Designator find(Named obj) {
    Designator ret = cache.get(obj);
    if (ret == null) {
      rebuild();
      ret = cache.get(obj);
    }
    return ret;
  }

  private void rebuild() {
    cache.clear();
    KnowPathTraverser traverser = new KnowPathTraverser(cache);
    traverser.visitItr(base.getRoot(), new Designator());
  }

  public void clear() {
    cache.clear();
  }

}

class KnowPathTraverser extends DefGTraverser<Void, Designator> {
  private Map<Named, Designator> cache;

  public KnowPathTraverser(Map<Named, Designator> cache) {
    super();
    this.cache = cache;
  }

  @Override
  protected Void visitItr(Iterable<? extends Fun> list, Designator param) {
    return super.visitItr(list, param);
  }

  @Override
  protected Void visit(Fun obj, Designator param) {
    if (obj instanceof Named) {
      if (cache.containsKey(obj)) {
        Designator oldparent = cache.get(obj);
        RError.err(ErrorType.Fatal, obj.getInfo(), "Same object (" + obj + ") found 2 times: " + oldparent + " and " + param);
      }
      cache.put((Named) obj, param);
      param = new Designator(param, ((Named) obj).getName());
      super.visit(obj, param);
    } else if ((obj instanceof Type) || (obj instanceof FunctionHeader)) { // FIXME remove hacky stuff
      super.visit(obj, param);
    }
    return null;
  }

  @Override
  protected Void visitFunctionHeader(FunctionHeader obj, Designator param) {
    return null;
  }

}
