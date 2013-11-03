package evl;

import common.ElementInfo;

public abstract class EvlBase implements Evl {

  private ElementInfo info;

  public EvlBase(ElementInfo info) {
    super();
    this.info = info;
  }

  public ElementInfo getInfo() {
    return info;
  }

}
