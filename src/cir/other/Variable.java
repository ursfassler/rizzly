package cir.other;

import cir.CirBase;
import cir.expression.reference.Referencable;
import cir.type.Type;

abstract public class Variable extends CirBase implements Referencable {
  private String name;
  private Type type;

  public Variable(String name, Type type) {
    super();
    this.name = name;
    this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public void setType(Type type) {
    this.type = type;
  }

}
