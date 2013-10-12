package fun.variable;

import common.ElementInfo;

import fun.FunBase;
import fun.expression.reference.Reference;
import fun.other.Named;

abstract public class Variable extends FunBase implements Named {
  private String name;
  private Reference type;

  public Variable(ElementInfo info, String name, Reference type) {
    super(info);
    this.name = name;
    this.type = type;
  }

  public Reference getType() {
    return type;
  }

  public void setType(Reference type) {
    this.type = type;
  }

  public String getName() {
    assert (name != null);
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name.toString() + ":" + type.toString();
  }
}
