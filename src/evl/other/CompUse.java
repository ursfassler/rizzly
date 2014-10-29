package evl.other;

import common.ElementInfo;

import evl.expression.reference.BaseRef;

public class CompUse extends BaseRef<Component> implements Named, Comparable<CompUse> {
  private String name;

  public CompUse(ElementInfo info, Component link, String name) {
    super(info, link);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int compareTo(CompUse o) {
    return name.compareTo(o.name);
  }

  @Override
  public String toString() {
    return name;
  }

}
