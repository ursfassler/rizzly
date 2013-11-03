package evl.composition;

import common.Designator;
import common.ElementInfo;

import evl.function.FuncIface;

final public class EndpointSelf extends Endpoint {

  private FuncIface iface;

  public EndpointSelf(ElementInfo info, FuncIface iface) {
    super(info);
    this.iface = iface;
  }

  public FuncIface getIface() {
    return iface;
  }

  public void setIface(FuncIface iface) {
    this.iface = iface;
  }

  @Override
  public FuncIface getIfaceUse() {
    return iface;
  }

  @Override
  public Designator getDes() {
    return new Designator(iface.getName());
  }

}
