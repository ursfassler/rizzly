package evl.type;

import common.ElementInfo;

import evl.EvlBase;
import evl.other.Named;

abstract public class Type extends EvlBase implements Named {
  private String name;

  public Type(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
