package fun.other;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import util.Pair;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.FunBase;

public class Namespace extends FunBase implements Named {
  private String name;
  final private FunList<Fun> children = new FunList<Fun>();

  public Namespace(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  public FunList<Fun> getChildren() {
    return children;
  }

  public Fun getChildItem(List<String> des) {
    LinkedList<String> ipath = new LinkedList<String>(des);
    Fun parent = this;

    while (!ipath.isEmpty()) {
      String name = ipath.pop();
      Fun child = ((Namespace) parent).getChildren().find(name);
      if (child == null) {
        RError.err(ErrorType.Error, parent.getInfo(), "Name not found: " + name);
      }
      parent = child;
    }
    return parent;
  }

  public Namespace forcePath(Designator des) {
    List<String> list = des.toList();
    list.remove(0);
    return forceChildPath(list);
  }

  public Namespace forceChildPath(List<String> des) {
    LinkedList<String> ipath = new LinkedList<String>(des);
    Namespace itr = this;

    while (!ipath.isEmpty()) {
      String ename = ipath.pop();
      itr = itr.force(ename);
    }
    return itr;
  }

  public Namespace force(String ename) {
    assert (findItem(ename) == null);
    Namespace ret = findSpace(ename);
    if (ret == null) {
      ret = new Namespace(ElementInfo.NO, ename);
      children.add(ret);
    }
    return ret;
  }

  public FunList<Namespace> getSpaces() {
    FunList<Namespace> ret = new FunList<Namespace>();
    for (Fun itr : children) {
      if (itr instanceof Namespace) {
        ret.add((Namespace) itr);
      }
    }
    return ret;
  }

  public FunList<Fun> getItems() {
    FunList<Fun> ret = new FunList<Fun>();
    for (Fun itr : children) {
      if (!(itr instanceof Namespace)) {
        ret.add(itr);
      }
    }
    return ret;
  }

  public Namespace findSpace(String name) {
    Fun ret = children.find(name);
    if (ret instanceof Namespace) {
      return (Namespace) ret;
    } else {
      return null;
    }
  }

  public Fun findItem(String name) {
    Fun ret = children.find(name);
    if (!(ret instanceof Namespace)) {
      return ret;
    } else {
      return null;
    }
  }

  public void addAll(Iterable<? extends Fun> list) {
    for (Fun itr : list) {
      if ((itr instanceof Named) && (children.find(((Named) itr).getName()) != null)) {
        throw new RuntimeException("element already exists: " + ((Named) itr).getName());
      } else {
        children.add(itr);
      }
    }
  }

  public void merge(Namespace space) {
    if (!name.equals(space.getName())) {
      throw new RuntimeException("names differ");
    }
    for (Fun itr : space.getItems()) {
      children.add(itr);
    }
    for (Namespace itr : space.getSpaces()) {
      Namespace sub = findSpace(itr.getName());
      if (sub == null) {
        children.add(itr);
      } else {
        sub.merge(itr);
      }
    }
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void subMerge(Named named) {
    if (named instanceof Namespace) {
      Namespace old = findSpace(named.getName());
      if (old != null) {
        old.merge((Namespace) named);
      } else {
        children.add(named);
      }
    } else {
      children.add(named);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Fun> void getItems(Class<T> kind, Designator prefix, Collection<Pair<Designator, T>> items) {
    for (Fun itr : getItems()) {
      if (kind.isAssignableFrom(itr.getClass())) {
        items.add(new Pair<Designator, T>(new Designator(prefix, ((Named) itr).getName()), (T) itr));
      }
    }
    for (Namespace itr : getSpaces()) {
      itr.getItems(kind, new Designator(prefix, itr.name), items);
    }
  }

  @Override
  public String toString() {
    return name;
  }

}
