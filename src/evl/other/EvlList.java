package evl.other;

import java.util.ArrayList;

import error.ErrorType;
import error.RError;
import evl.Evl;

public class EvlList<T extends Evl> extends ArrayList<T> {
  private static final long serialVersionUID = 2599344789209692487L;

  public EvlList() {
  }

  public EvlList(EvlList<? extends T> list) {
    super(list);
  }

  public boolean add(T item) {
    return super.add(item);
  }

  public T find(String name) {
    EvlList<T> ret = new EvlList<T>();
    for (T itr : this) {
      if (itr instanceof Named) {
        if (((Named) itr).getName().equals(name)) {
          ret.add(itr);
        }
      }
    }

    switch (ret.size()) {
      case 0:
        return null;
      case 1:
        return ret.get(0);
      default:
        for (T itr : ret) {
          RError.err(ErrorType.Hint, itr.getInfo(), "this");
        }
        RError.err(ErrorType.Fatal, "Found more than one entry with name " + name);
        return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <R extends T> EvlList<R> getItems(Class<R> kind) {
    EvlList<R> ret = new EvlList<R>();
    for (T itr : this) {
      if (kind.isAssignableFrom(itr.getClass())) {
        ret.add((R) itr);
      }
    }
    return ret;
  }

}
