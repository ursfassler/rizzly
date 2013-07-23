package fun.expression.reference;

import common.ElementInfo;

final public class RefName extends RefItem {
  final private String name;

  public RefName(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "." + name;
  }

}
