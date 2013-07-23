package cir.type;

import cir.CirBase;
import cir.expression.reference.Referencable;

public class EnumElement extends CirBase implements Referencable {
  private String name;
  private int value;
  private Type type;

  public EnumElement(String name, Type type, int value) {
    this.name = name;
    this.value = value;
    this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  public int getValue() {
    return value;
  }

  public Type getType() {
    return type;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public void setType(Type type) {
    this.type = type;
  }

}
