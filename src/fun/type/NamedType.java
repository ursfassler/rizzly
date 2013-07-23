package fun.type;

import common.ElementInfo;

import fun.other.Named;

public class NamedType extends Type implements Named {
  private String name;
  private Type type;

  public NamedType(ElementInfo info, String name, Type type) {
    super(info);
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return name;
  }

}
