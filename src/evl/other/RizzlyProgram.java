package evl.other;

import common.ElementInfo;

import evl.EvlBase;
import evl.function.Function;
import evl.type.Type;
import evl.variable.Constant;
import evl.variable.StateVariable;

public class RizzlyProgram extends EvlBase {
  private String name;
  private EvlList<Type> type = new EvlList<Type>();
  private EvlList<StateVariable> variable = new EvlList<StateVariable>();
  private EvlList<Constant> constant = new EvlList<Constant>();
  private EvlList<Function> function = new EvlList<Function>();

  public RizzlyProgram(String name) {
    super(ElementInfo.NO);
    this.name = name;
  }

  public EvlList<Type> getType() {
    return type;
  }

  public EvlList<StateVariable> getVariable() {
    return variable;
  }

  public EvlList<Function> getFunction() {
    return function;
  }

  public EvlList<Constant> getConstant() {
    return constant;
  }

  public String getName() {
    return name;
  }

}
