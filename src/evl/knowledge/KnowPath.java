package evl.knowledge;

import java.util.HashMap;
import java.util.Map;

import common.Designator;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.function.FunctionBase;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.Namespace;
import evl.type.base.EnumType;

public class KnowPath extends KnowledgeEntry {
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

class KnowPathTraverser extends DefTraverser<Void, Designator> {
  private Map<Named, Designator> cache;

  public KnowPathTraverser(Map<Named, Designator> cache) {
    super();
    this.cache = cache;
  }

  @Override
  protected Void visitItr(Iterable<? extends Evl> list, Designator param) {
    return super.visitItr(list, param);
  }

  @Override
  protected Void visit(Evl obj, Designator param) {
    if (obj instanceof Named) {
      if (cache.containsKey(obj)) {
        Designator oldparent = cache.get(obj);
        RError.err(ErrorType.Fatal, obj.getInfo(), "Same object (" + obj + ") found 2 times: " + oldparent + " and " + param);
      }
      cache.put((Named) obj, param);
      super.visit(obj, param);
    }
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Designator param) {
    return super.visitNamespace(obj, new Designator(param, obj.getName()));
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Designator param) {
    return super.visitImplElementary(obj, new Designator(param, obj.getName()));
  }

  @Override
  protected Void visitEnumType(EnumType obj, Designator param) {
    return super.visitEnumType(obj, new Designator(param, obj.getName()));
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Designator param) {
    return null;
  }

}
