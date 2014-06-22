package evl.composition;

import common.Designator;
import common.ElementInfo;

import evl.function.FuncIface;
import evl.other.CompUse;

public class EndpointSub extends Endpoint {
  private CompUse comp;
  private String iface;

  public EndpointSub(ElementInfo info, CompUse comp, String iface) {
    super(info);
    this.comp = comp;
    this.iface = iface;
  }

  public CompUse getComp() {
    return comp;
  }

  public void setComp(CompUse comp) {
    this.comp = comp;
  }

  public String getIface() {
    return iface;
  }

  public void setIface(String iface) {
    this.iface = iface;
  }

  @Override
  public FuncIface getIfaceUse() {
    FuncIface ret = comp.getLink().getIface().find(iface);
    assert (ret != null);
    return ret;
  }

  @Override
  public Designator getDes() {
    return new Designator(comp.getName(), iface);
  }

  @Override
  public String toString() {
    return comp.getName() + "." + iface;
  }

}
