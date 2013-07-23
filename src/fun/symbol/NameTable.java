package fun.symbol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import common.Designator;

import error.ErrorType;
import error.RError;

/**
 * 
 * @author urs
 */
public class NameTable {
  private Set<Designator> names = new HashSet<Designator>();
  private Map<String, Designator> alias = new HashMap<String, Designator>();
  private Map<String, Set<Designator>> ambigious = new HashMap<String, Set<Designator>>();

  public void addShort(String shorter, Designator name) {
    assert (names.contains(name));

    if (name.toList().size() > 1) {
      if (alias.containsKey(shorter)) {
        assert (!ambigious.containsKey(shorter));
        Designator old = alias.get(shorter);
        assert (old != null);
        assert (names.contains(old));

        Set<Designator> set = new HashSet<Designator>();
        set.add(old);
        set.add(name);

        ambigious.put(shorter, set);
        alias.remove(shorter);
      } else if (ambigious.containsKey(shorter)) {
        Set<Designator> set = ambigious.get(shorter);
        set.add(name);
      } else {
        alias.put(shorter, name);
      }
    }
  }

  public void addName(Designator name) {
    assert (!name.toList().isEmpty());
    if (names.contains(name)) {
      RError.err(ErrorType.Error, "Name \"" + name + "\" already defined");
    } else {
      assert (!ambigious.containsKey(name));
      names.add(name);
    }
  }

  public Designator expand(String name) {
    if (names.contains(new Designator(name))) {
      return new Designator(name);
    } else if (alias.containsKey(name)) {
      Designator full = alias.get(name);
      assert (full != null);
      assert (names.contains(full));
      return full;
    } else if (ambigious.containsKey(name)) {
      Set<Designator> defs = ambigious.get(name);
      for (Designator itr : defs) {
        RError.err(ErrorType.Hint, "Name option: " + itr.toString());
      }
      RError.err(ErrorType.Error, "Name \"" + name.toString() + "\" is ambigous");
      return null;
    } else {
      RError.err(ErrorType.Error, "Unknown name: " + name.toString());
      return null;
    }
  }

  public boolean hasName(Designator name) {
    return names.contains(name);
  }

  public Set<Designator> getNames() {
    return names;
  }

  public Map<String, Designator> getAlias() {
    return alias;
  }

}
