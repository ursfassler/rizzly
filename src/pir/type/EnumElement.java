package pir.type;

import pir.PirObject;
import pir.expression.reference.Referencable;

final public class EnumElement extends PirObject implements Referencable {
  private String name;
  private EnumType type;

  public EnumElement(String name, EnumType type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EnumType getType() {
    return type;
  }

  public void setType(EnumType type) {
    this.type = type;
  }
  
  public int getIntValue(){
    int intValue = type.getElements().indexOf(this);
    assert (intValue >= 0);
    return intValue;
  }

  @Override
  public String toString() {
    return name;
  }

}
