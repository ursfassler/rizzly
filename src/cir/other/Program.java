package cir.other;

import java.util.ArrayList;
import java.util.List;

import cir.CirBase;
import cir.function.Function;
import cir.function.LibFunction;
import cir.library.CLibrary;
import cir.type.Type;

public class Program extends CirBase {
  final private String name;
  final private List<Type> type = new ArrayList<Type>();
  final private List<Variable> variable = new ArrayList<Variable>();
  final private List<Function> function = new ArrayList<Function>();
  final private List<CLibrary> library = new ArrayList<CLibrary>();

  public Program(String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public List<Type> getType() {
    return type;
  }

  public List<Variable> getVariable() {
    return variable;
  }

  public List<Function> getFunction() {
    return function;
  }

  public List<CLibrary> getLibrary() {
    return library;
  }

  public LibFunction getLibFunc(String library, String function) {
    for (CLibrary lib : this.library) {
      if (lib.getName().equals(library)) {
        LibFunction func = lib.find(function);
        if (func != null) {
          return func;
        } else {
          break;
        }
      }
    }
    throw new RuntimeException("Library function not found: " + library + "." + function);
  }

}
