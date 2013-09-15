package fun.type.base;

import common.ElementInfo;

import fun.FunBase;
import fun.other.Named;

final public class EnumElement extends FunBase implements Named {
  private String name;

  public EnumElement(ElementInfo info, String name) {
    super(info);
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
