package pir.other;

import java.util.ArrayList;
import java.util.List;

import pir.PirObject;
import pir.function.Function;
import pir.type.Type;

final public class Program extends PirObject {

  final private String name;
  final private List<Type> type = new ArrayList<Type>();
  final private List<StateVariable> variable = new ArrayList<StateVariable>();
  final private List<Constant> constant = new ArrayList<Constant>();
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

  public List<StateVariable> getVariable() {
    return variable;
  }

  public List<Constant> getConstant() {
    return constant;
  }

  public List<Function> getFunction() {
    return function;
  }
}
