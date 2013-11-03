package common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Designator implements Iterable<String> {
  final static public String NAME_SEP = "_";
  final private ArrayList<String> name;

  public Designator() {
    this.name = new ArrayList<String>();
  }

  public Designator(List<String> name) {
    this.name = new ArrayList<String>(name);
  }

  public Designator(String name) {
    this.name = new ArrayList<String>(1);
    this.name.add(name);
  }

  public Designator(String name0, String name1) {
    this.name = new ArrayList<String>(2);
    this.name.add(name0);
    this.name.add(name1);
  }

  public Designator(Designator old) {
    this.name = new ArrayList<String>(old.name);
  }

  public Designator(Designator des, String name) {
    this.name = new ArrayList<String>(des.name);
    this.name.add(name);
  }

  public Designator(Designator des, List<String> suffix) {
    this.name = new ArrayList<String>(des.name);
    this.name.addAll(suffix);
  }

  public Designator(String prefix, Designator suffix) {
    this.name = new ArrayList<String>(suffix.name);
    this.name.add(0, prefix);
  }

  public int size() {
    return name.size();
  }

  public ArrayList<String> toList() {
    return new ArrayList<String>(name);
  }

  public String toString(String separator) {
    String res = "";
    for (int i = 0; i < name.size(); i++) {
      if (i > 0) {
        res += separator;
      }
      res += name.get(i);
    }
    return res;
  }

  @Override
  public String toString() {
    return toString(".");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Designator other = (Designator) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  public Iterator<String> iterator() {
    return name.iterator();
  }
}
