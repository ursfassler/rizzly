package evl.type.composed;

import common.ElementInfo;

import evl.EvlBase;
import evl.other.Named;
import evl.type.TypeRef;

public class NamedElement extends EvlBase implements Named {
  private String name;
  private TypeRef type;

  public NamedElement(ElementInfo info, String name, TypeRef type) {
    super(info);
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TypeRef getType() {
    return type;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return name + ":" + type.toString();
  }

}
