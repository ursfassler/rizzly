package evl.variable;

import common.ElementInfo;

import evl.EvlBase;
import evl.other.Named;
import evl.type.Type;

abstract public class Variable extends EvlBase implements Named {
  private String name;
  private Type type;

  public Variable(ElementInfo info, String name, Type type) {
    super(info);
    assert (name != null);
    this.name = name;
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    assert (name != null);
    this.name = name;
  }

  @Override
  public String toString() {
    return name.toString() + ":" + type.toString();
  }
}
