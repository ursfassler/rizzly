package cir.library;

import java.util.ArrayList;
import java.util.List;

import cir.CirBase;
import cir.function.LibFunction;

public class CLibrary extends CirBase {
  private final String name;
  private final List<LibFunction> function = new ArrayList<LibFunction>();

  public CLibrary(String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public List<LibFunction> getFunction() {
    return function;
  }

  public LibFunction find(String name) {
    for (LibFunction func : function) {
      if (func.getName().equals(name)) {
        return func;
      }
    }
    return null;
  }

}
