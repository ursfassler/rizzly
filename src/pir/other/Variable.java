package pir.other;

import pir.PirObject;
import pir.expression.reference.Referencable;
import pir.type.TypeRef;

abstract public class Variable extends PirObject implements Referencable {
  private String name;
  private TypeRef type;

  public Variable(String name, TypeRef type) {
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

  @Override
  public String toString() {
    return name + ":" + type.getRef().getName();
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }

}
