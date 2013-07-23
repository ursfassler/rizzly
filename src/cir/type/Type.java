package cir.type;

import cir.CirBase;
import cir.expression.reference.Referencable;

abstract public class Type extends CirBase implements Referencable {
  private String name;

  public Type(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

}
