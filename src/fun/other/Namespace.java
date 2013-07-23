package fun.other;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;

public class Namespace extends ListOfNamed<Named> implements Named {
  final private ElementInfo info;
  private String name;

  public Namespace(ElementInfo info, String name) {
    super();
    this.info = info;
    this.name = name;
  }

  public Named getChildItem(List<String> des) {
    LinkedList<String> ipath = new LinkedList<String>(des);
    Named parent = this;

    while (!ipath.isEmpty()) {
      String name = ipath.pop();
      Named child = ((Namespace) parent).find(name);
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
      ret = new Namespace(new ElementInfo(), ename);
      add(ret);
    }
    return ret;
  }

  public void add(Namespace space) {
    assert (find(space.getName()) == null);
    super.add(space);
  }

  public void add(Named item) {
    assert (find(item.getName()) == null);
    super.add(item);
  }

  public String getName() {
    return name;
  }

  public List<Namespace> getSpaces() {
    List<Namespace> ret = new ArrayList<Namespace>();
    for (Named itr : this) {
      if (itr instanceof Namespace) {
        ret.add((Namespace) itr);
      }
    }
    return ret;
  }

  public List<Named> getItems() {
    List<Named> ret = new ArrayList<Named>();
    for (Named itr : this) {
      if (!(itr instanceof Namespace)) {
        ret.add(itr);
      }
    }
    return ret;
  }

  public Named find(String name) {
    for (Named itr : this) {
      if (itr.getName().equals(name)) {
        return itr;
      }
    }
    return null;
  }

  public Namespace findSpace(String name) {
    Named ret = find(name);
    if (ret instanceof Namespace) {
      return (Namespace) ret;
    } else {
      return null;
    }
  }

  public Named findItem(String name) {
    Named ret = find(name);
    if (!(ret instanceof Namespace)) {
      return ret;
    } else {
      return null;
    }
  }

  public void addAll(Iterable<? extends Named> list) {
    for (Named itr : list) {
      if (find(itr.getName()) != null) {
        throw new RuntimeException("element already exists: " + itr.getName());
      } else {
        super.add(itr);
      }
    }
  }

  public void merge(Namespace space) {
    if (!name.equals(space.getName())) {
      throw new RuntimeException("names differ");
    }
    for (Named itr : space.getItems()) {
      add(itr);
    }
    for (Namespace itr : space.getSpaces()) {
      Namespace sub = findSpace(itr.getName());
      if (sub == null) {
        add(itr);
      } else {
        sub.merge(itr);
      }
    }
  }

  @Override
  public ElementInfo getInfo() {
    return info;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public void subMerge(Named named) {
    if (named instanceof Namespace) {
      Namespace old = findSpace(named.getName());
      if (old != null) {
        old.merge((Namespace) named);
      } else {
        add(named);
      }
    } else {
      add(named);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Named> List<T> getItems(Class<T> kind, boolean recursive) {
    List<T> ret = new ArrayList<T>();
    for (Named itr : getItems()) {
      if (kind.isAssignableFrom(itr.getClass())) {
        ret.add((T) itr);
      }
    }
    if (recursive) {
      for (Namespace itr : getSpaces()) {
        ret.addAll(itr.getItems(kind, true));
      }
    }
    return ret;
  }

  @Override
  public String toString() {
    return name;
  }

}
