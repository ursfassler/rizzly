package evl.other;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ListOfNamed<T extends Named> implements Iterable<T> {
  final private List<T> list;

  public ListOfNamed(Collection<T> itemList) {
    list = new ArrayList<T>(itemList);
  }

  public ListOfNamed() {
    list = new ArrayList<T>();
  }

  public List<T> getList() {
    return list;
  }

  public T find(String name) {
    for (T itr : list) {
      if (itr.getName().equals(name)) {
        return itr;
      }
    }
    return null;
  }

  public void add(T item) {
    list.add(item);
  }

  public void addAll(Iterable<? extends T> list) {
    for (T itr : list) {
      add(itr);
    }
  }

  @Override
  public Iterator<T> iterator() {
    return list.iterator();
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public void clear() {
    list.clear();
  }

  public boolean removeAll(Collection<?> c) {
    return list.removeAll(c);
  }

  public boolean retainAll(Collection<?> c) {
    return list.retainAll(c);
  }

  public T remove(int index) {
    return list.remove(index);
  }

  public boolean remove(Object o) {
    return list.remove(o);
  }

  public int size() {
    return list.size();
  }

  @SuppressWarnings("unchecked")
  public <R extends T> List<R> getItems(Class<R> kind) {
    List<R> ret = new ArrayList<R>();
    for (Named itr : list) {
      if (kind.isAssignableFrom(itr.getClass())) {
        ret.add((R) itr);
      }
    }
    return ret;
  }

  @Override
  public String toString() {
    return list.toString();
  }

}
