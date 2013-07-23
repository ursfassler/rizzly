package evl.composition;

import common.Designator;
import common.Direction;
import common.ElementInfo;

import evl.other.CompUse;
import evl.other.IfaceUse;

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
  public IfaceUse getIfaceUse() {
    IfaceUse ret = comp.getLink().getIface(Direction.in).find(iface);
    if (ret == null) {
      ret = comp.getLink().getIface(Direction.out).find(iface);
    }
    assert (ret != null);
    return ret;
  }

  @Override
  public Designator getDes() {
    return new Designator( comp.getName(), iface );
  }

}
