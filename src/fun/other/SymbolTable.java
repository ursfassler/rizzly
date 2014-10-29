package fun.other;

import java.util.Collection;
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
public class SymbolTable {

  private SymbolTable parent;
  private Map<String, Named> entries = new HashMap<String, Named>();

  public SymbolTable(SymbolTable parent) {
    this.parent = parent;
  }

  public SymbolTable() {
    this.parent = null;
  }

  public Named get(String name, ElementInfo info) {
    Named res = find(name, true);
    if (res == null) {
      RError.err(ErrorType.Error, info, "Entry not found: " + name);
      return null;
    }
    return res;
  }

  public Named find(String name, boolean recursive) {
    if (entries.containsKey(name)) {
      return entries.get(name);
    } else if (recursive && (parent != null)) {
      return parent.find(name, recursive);
    } else {
      return null;
    }
  }

  public Named find(String name) {
    return find(name, true);
  }

  public void addAll(Collection<? extends Fun> syms) {
    for (Fun itr : syms) {
      add(itr);
    }
  }

  public void add(Named obj) {
    Fun old = find(obj.getName(), false);
    if (old != null) {
      if (old instanceof Fun) {
        RError.err(ErrorType.Hint, old.getInfo(), "First definition was here");
        RError.err(ErrorType.Error, obj.getInfo(), "Entry already defined: " + obj.getName());
      } else {
        RError.err(ErrorType.Error, "Entry already defined: " + obj.getName());
      }
    }
    entries.put(obj.getName(), obj);
  }

  public void add(Fun obj) {
    if (obj instanceof Named) {
      add((Named) obj);
    }
  }

  public SymbolTable getParent() {
    return parent;
  }

}
