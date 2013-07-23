package evl.composition;

import common.Designator;
import common.ElementInfo;

import evl.EvlBase;
import evl.other.IfaceUse;

abstract public class Endpoint extends EvlBase {

  public Endpoint(ElementInfo info) {
    super(info);
  }

  abstract public IfaceUse getIfaceUse();

  abstract public Designator getDes();
}
