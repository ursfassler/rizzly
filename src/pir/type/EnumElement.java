package pir.type;

import pir.PirObject;

final public class EnumElement extends PirObject {

  private String name;

  public EnumElement(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
