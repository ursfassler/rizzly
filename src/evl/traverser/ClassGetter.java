package evl.traverser;

import java.util.ArrayList;
import java.util.List;

import evl.DefTraverser;
import evl.Evl;

public class ClassGetter<T extends Evl> extends DefTraverser<Void, Void> {
  private List<T> ret = new ArrayList<T>();
  private Class<T> kind;

  public ClassGetter(Class<T> kind) {
    super();
    this.kind = kind;
  }

  static public <T extends Evl> List<T> get(Class<T> kind, Evl root) {
    ClassGetter<T> getter = new ClassGetter<T>(kind);
    getter.traverse(root, null);
    return getter.ret;
  }

  static public <T extends Evl> List<T> getAll(Class<T> kind, Iterable<? extends Evl> root) {
    ClassGetter<T> getter = new ClassGetter<T>(kind);
    for (Evl itr : root) {
      getter.traverse(itr, null);
    }
    return getter.ret;
  }

  @Override
  protected Void visit(Evl obj, Void param) {
    if (kind.isAssignableFrom(obj.getClass())) {
      ret.add((T) obj);
    }
    return super.visit(obj, param);
  }

}
