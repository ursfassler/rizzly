package evl.composition;

import common.Designator;
import common.ElementInfo;

import evl.EvlBase;
import evl.function.FuncIface;

abstract public class Endpoint extends EvlBase {

  public Endpoint(ElementInfo info) {
    super(info);
  }

  abstract public FuncIface getIfaceUse();

  abstract public Designator getDes();
}
