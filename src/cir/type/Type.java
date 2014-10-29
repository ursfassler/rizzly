package cir.type;

import cir.CirBase;
import cir.expression.reference.Referencable;

abstract public class Type extends CirBase implements Referencable {
  private String name;

  public Type(String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
