package fun.type;

import common.ElementInfo;

import fun.FunBase;
import fun.other.ActualTemplateArgument;
import fun.other.Named;

abstract public class Type extends FunBase implements Named, ActualTemplateArgument {
  private String name;

  public Type(ElementInfo info, String name) {
    super(info);
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
