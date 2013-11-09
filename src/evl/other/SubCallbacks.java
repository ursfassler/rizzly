package evl.other;

import common.ElementInfo;

import evl.Evl;
import evl.function.FunctionHeader;

public class SubCallbacks extends ListOfNamed<FunctionHeader> implements Named, Evl {
  private ElementInfo info;
  private String name;

  public SubCallbacks(ElementInfo info, String name) {
    this.info = info;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public ElementInfo getInfo() {
    return info;
  }

  @Override
  public String toString() {
    return name;
  }

}
