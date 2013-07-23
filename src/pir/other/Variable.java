package pir.other;

import pir.PirObject;
import pir.expression.reference.Referencable;
import pir.type.Type;

abstract public class Variable extends PirObject implements Referencable {
  private String name;
  private Type type;

  public Variable(String name, Type type) {
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

  @Override
  public String toString() {
    return name + ":" + type.getName();
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(Type type) {
    this.type = type;
  }

}
