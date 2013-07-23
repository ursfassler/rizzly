package evl.type.composed;

import common.ElementInfo;

import evl.EvlBase;
import evl.other.Named;
import evl.type.Type;

public class NamedElement extends EvlBase implements Named {
  private String name;
  private Type type;

  public NamedElement(ElementInfo info, String name, Type type) {
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

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return name + ":" + type.toString();
  }

}
