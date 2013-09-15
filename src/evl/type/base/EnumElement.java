package evl.type.base;

import common.ElementInfo;

import evl.EvlBase;
import evl.other.Named;

final public class EnumElement extends EvlBase implements Named {

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
