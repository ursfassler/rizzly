package cir.variable;

import cir.CirBase;
import cir.expression.reference.Referencable;
import cir.other.Named;
import cir.type.TypeRef;

abstract public class Variable extends CirBase implements Named, Referencable {
  private String name;
  private TypeRef type;

  public Variable(String name, TypeRef type) {
    super();
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

  public void setName(String name) {
    this.name = name;
  }

}
