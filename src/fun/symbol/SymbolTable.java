package fun.symbol;

import java.util.HashMap;
import java.util.Map;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.Fun;

/**
 *
 * @author urs
 */
public class SymbolTable<T, N> {

  private SymbolTable<T, N> parent;
  private HashMap<N, T> entries = new HashMap<N, T>();

  public SymbolTable(SymbolTable<T, N> parent) {
    this.parent = parent;
  }

  public SymbolTable() {
    this.parent = null;
  }

  public T get(N name, ElementInfo info) {
    T res = find(name, true);
    if (res == null) {
      RError.err(ErrorType.Error, info, "Entry not found: " + name);
      return null;
    }
    return res;
  }

  public T find(N name, boolean recursive) {
    if (entries.containsKey(name)) {
      return entries.get(name);
    } else if (recursive && (parent != null)) {
      return parent.find(name, recursive);
    } else {
      return null;
    }
  }

  public T find(N name) {
    return find(name, true);
  }

  public void add(N name, T sym) {
    T old = find(name, false);
    if (old != null) {
      if (old instanceof Fun) {
        RError.err(ErrorType.Hint, ((Fun) old).getInfo(), "First definition was here");
        RError.err(ErrorType.Error, ((Fun) sym).getInfo(), "Entry already defined: " + name);
      } else {
        RError.err(ErrorType.Error, "Entry already defined: " + name);
      }
    }
    entries.put(name, sym);
  }

  public SymbolTable<T, N> getParent() {
    return parent;
  }

  public Map<N, T> getMap() {
    Map<N, T> ret;
    if (parent == null) {
      ret = new HashMap<N, T>();
    } else {
      ret = parent.getMap();
    }
    ret.putAll(entries);
    return ret;
  }

}
