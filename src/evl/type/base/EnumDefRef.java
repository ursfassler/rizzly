package evl.type.base;

import common.ElementInfo;

import evl.EvlBase;

public class EnumDefRef extends EvlBase {
  private EnumElement elem;

  public EnumDefRef(ElementInfo info, EnumElement elem) {
    super(info);
    this.elem = elem;
  }

  public EnumElement getElem() {
    return elem;
  }

  public void setElem(EnumElement elem) {
    this.elem = elem;
  }

  @Override
  public String toString() {
    return elem.toString();
  }

}
