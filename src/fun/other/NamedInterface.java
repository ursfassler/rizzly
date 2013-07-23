package fun.other;

import common.ElementInfo;

import fun.FunBase;

public class NamedInterface extends FunBase implements Named {
  private String name;
  private Interface iface;

  public NamedInterface(ElementInfo info, String name, Interface iface) {
    super(info);
    this.name = name;
    this.iface = iface;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public Interface getIface() {
    return iface;
  }

  public void setIface(Interface iface) {
    this.iface = iface;
  }

  @Override
  public String toString() {
    return name;
  }

}
