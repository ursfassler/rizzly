package pir.type;

import pir.PirObject;

public class NamedElement extends PirObject {
  private String name;
  private TypeRef type;

  public NamedElement(String name, TypeRef type) {
    super();
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public TypeRef getType() {
    return type;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return name + ":" + type.toString();
  }

}
