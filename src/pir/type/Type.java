package pir.type;

import pir.PirObject;
import pir.expression.reference.Referencable;

abstract public class Type extends PirObject implements Referencable {
  final private String name;

  public Type(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

}
