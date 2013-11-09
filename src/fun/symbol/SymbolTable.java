package fun.symbol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.other.Named;

/**
 * 
 * @author urs
 */
public class SymbolTable {

  private SymbolTable parent;
  private HashMap<String, Named> entries = new HashMap<String, Named>();

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

  public void addAll(Collection<? extends Named> syms) {
    for (Named itr : syms) {
      add(itr);
    }
  }

  public void add(Named sym) {
    Named old = find(sym.getName(), false);
    if (old != null) {
      if (old instanceof Fun) {
        RError.err(ErrorType.Hint, ((Fun) old).getInfo(), "First definition was here");
        RError.err(ErrorType.Error, ((Fun) sym).getInfo(), "Entry already defined: " + sym.getName());
      } else {
        RError.err(ErrorType.Error, "Entry already defined: " + sym.getName());
      }
    }
    entries.put(sym.getName(), sym);
  }

  public SymbolTable getParent() {
    return parent;
  }

  public Map<String, Named> getMap() {
    Map<String, Named> ret;
    if (parent == null) {
      ret = new HashMap<String, Named>();
    } else {
      ret = parent.getMap();
    }
    ret.putAll(entries);
    return ret;
  }

}
