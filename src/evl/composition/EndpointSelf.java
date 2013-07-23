package evl.composition;

import common.Designator;
import common.ElementInfo;

import evl.other.IfaceUse;

final public class EndpointSelf extends Endpoint {

  private IfaceUse iface;

  public EndpointSelf(ElementInfo info, IfaceUse iface) {
    super(info);
    this.iface = iface;
  }

  public IfaceUse getIface() {
    return iface;
  }

  public void setIface(IfaceUse iface) {
    this.iface = iface;
  }

  @Override
  public IfaceUse getIfaceUse() {
    return iface;
  }

  @Override
  public Designator getDes() {
    return new Designator(iface.getName());
  }

}
