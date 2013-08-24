package evl.variable;

import common.ElementInfo;

import evl.EvlBase;
import evl.other.Named;
import evl.type.TypeRef;

abstract public class Variable extends EvlBase implements Named {
  private String name;
  private TypeRef type;

  public Variable(ElementInfo info, String name, TypeRef type) {
    super(info);
    assert (name != null);
    this.name = name;
    this.type = type;
  }

  public TypeRef getType() {
    return type;
  }

  public void setType(TypeRef type) {
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
    return name.toString() + ":" + type.getRef().getName();
  }
}
