package evl.other;

import common.ElementInfo;

import evl.Evl;

public class NamedList<T extends Named> extends ListOfNamed<T> implements Named, Evl {
  private ElementInfo info;
  private String name;

  public NamedList(ElementInfo info, String name) {
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
