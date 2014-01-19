package cir.other;

import java.util.ArrayList;
import java.util.List;

import cir.CirBase;
import cir.function.Function;
import cir.type.Type;

public class Program extends CirBase {
  final private String name;
  final private List<Type> type = new ArrayList<Type>();
  final private List<Variable> variable = new ArrayList<Variable>();
  final private List<Function> function = new ArrayList<Function>();

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

}
