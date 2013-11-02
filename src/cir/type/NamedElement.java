package cir.type;

import cir.CirBase;

public class NamedElement extends CirBase {
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

}
