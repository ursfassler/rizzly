package fun.other;

import common.ElementInfo;

import fun.FunBase;

public class NamedComponent extends FunBase implements Named {
  private String name;
  private Component comp;

  public NamedComponent(ElementInfo info, String name, Component comp) {
    super(info);
    this.name = name;
    this.comp = comp;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Component getComp() {
    return comp;
  }

  public void setComp(Component comp) {
    this.comp = comp;
  }

  @Override
  public String toString() {
    return name;
  }

}
