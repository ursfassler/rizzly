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
    assert (type != null);
    this.type = type;
    this.name = name;
  }

  public Reference getType() {
    return type;
  }

  public void setType(Reference type) {
    assert (type != null);
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name + ":" + type.toString();
  }
}
