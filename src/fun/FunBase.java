package fun;

import common.ElementInfo;

public abstract class FunBase implements Fun {

  private ElementInfo info;

  public FunBase(ElementInfo info) {
    super();
    this.info = info;
  }

  public ElementInfo getInfo() {
    return info;
  }

  
  
}
