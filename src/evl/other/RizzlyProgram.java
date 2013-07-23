package evl.other;

import common.ElementInfo;

import evl.Evl;
import evl.function.FunctionBase;
import evl.type.Type;
import evl.variable.Constant;
import evl.variable.StateVariable;

public class RizzlyProgram implements Evl {
  private String rootdir;
  private String name;
  private ListOfNamed<Type> type = new ListOfNamed<Type>();
  private ListOfNamed<StateVariable> variable = new ListOfNamed<StateVariable>();
  private ListOfNamed<Constant> constant = new ListOfNamed<Constant>();
  private ListOfNamed<FunctionBase> function = new ListOfNamed<FunctionBase>();

  public RizzlyProgram(String rootdir, String name) {
    super();
    this.rootdir = rootdir;
    this.name = name;
  }

  public String getRootdir() {
    return rootdir;
  }

  public ElementInfo getInfo() {
    throw new RuntimeException("not yet implemented");
  }

  public ListOfNamed<Type> getType() {
    return type;
  }

  public ListOfNamed<StateVariable> getVariable() {
    return variable;
  }

  public ListOfNamed<FunctionBase> getFunction() {
    return function;
  }

  public ListOfNamed<Constant> getConstant() {
    return constant;
  }

  public String getName() {
    return name;
  }

}
