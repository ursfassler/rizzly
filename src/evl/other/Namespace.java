package evl.other;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.EvlBase;

public class Namespace extends EvlBase implements Named {
  private String name;
  final private EvlList<Evl> children = new EvlList<Evl>();

  public Namespace(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  public EvlList<Evl> getChildren() {
    return children;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
    Namespace ret = findSpace(ename);
    if (ret == null) {
      assert (children.find(ename) == null);
      ret = new Namespace(ElementInfo.NO, ename);
      add(ret);
    }
    return ret;
  }

  public void add(Evl evl) {
    children.add(evl);
  }

  public EvlList<Namespace> getSpaces() {
    EvlList<Namespace> ret = new EvlList<Namespace>();
    for (Evl itr : children) {
      if (itr instanceof Namespace) {
        ret.add((Namespace) itr);
      }
    }
    return ret;
  }

  public EvlList<Evl> getItems() {
    EvlList<Evl> ret = new EvlList<Evl>();
    for (Evl itr : children) {
      if (!(itr instanceof Namespace)) {
        ret.add(itr);
      }
    }
    return ret;
  }

  public Namespace findSpace(String name) {
    Evl ret = children.find(name);
    if (ret instanceof Namespace) {
      return (Namespace) ret;
    } else {
      return null;
    }
  }

  public Evl findItem(String name) {
    Evl ret = children.find(name);
    if (!(ret instanceof Namespace)) {
      return ret;
    } else {
      return null;
    }
  }

  public void addAll(Collection<? extends Evl> list) {
    children.addAll(list);
  }

  public void merge(Namespace space) {
    if (!name.equals(space.getName())) {
      throw new RuntimeException("names differ");
    }
    for (Evl itr : space.getItems()) {
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
  public <T extends Evl> List<T> getItems(Class<T> kind, boolean recursive) {
    List<T> ret = new ArrayList<T>();
    for (Evl itr : getItems()) {
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

  public Evl getChildItem(List<String> des) {
    LinkedList<String> ipath = new LinkedList<String>(des);
    Evl parent = this;

    while (!ipath.isEmpty()) {
      String name = ipath.pop();
      Evl child = ((Namespace) parent).getChildren().find(name);
      if (child == null) {
        RError.err(ErrorType.Error, parent.getInfo(), "Name not found: " + name);
      }
      parent = child;
    }
    return parent;
  }

  @Override
  public String toString() {
    return name;
  }

}
