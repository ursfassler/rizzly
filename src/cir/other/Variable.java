package cir.other;

import cir.CirBase;
import cir.expression.reference.Referencable;
import cir.type.TypeRef;

abstract public class Variable extends CirBase implements Referencable {
  private String name;
  private TypeRef type;

  public Variable(String name, TypeRef type) {
    super();
    this.name = name;
    this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  public TypeRef getType() {
    return type;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }

}
