package pir.type;

import pir.PirObject;

public class NamedElement extends PirObject {
  private String name;
  private Type type;

  public NamedElement(String name, Type type) {
    super();
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return name + ":" + type.getName();
  }

}
