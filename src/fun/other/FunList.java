package fun.other;

import java.util.ArrayList;
import java.util.List;

import fun.Fun;

public class FunList<T extends Fun> extends ArrayList<T> {

  public FunList(FunList<? extends T> list) {
    super(list);
  }

  public FunList() {
    super();
  }

  @SuppressWarnings("unchecked")
  public <R extends T> FunList<R> getItems(Class<R> kind) {
    FunList<R> ret = new FunList<R>();
    for (T itr : this) {
      if (kind.isAssignableFrom(itr.getClass())) {
        ret.add((R) itr);
      }
    }
    return ret;
  }

  public List<String> names() {
    List<String> names = new ArrayList<String>();
    for (T itr : this) {
      if (itr instanceof Named) {
        names.add(((Named) itr).getName());
      }
    }
    return names;
  }

  public Named find(String name) {
    for (T itr : this) {
      if (itr instanceof Named) {
        if (((Named) itr).getName().equals(name)) {
          return (Named) itr;
        }
      }
    }
    return null;
  }
}
