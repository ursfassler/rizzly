package fun.type.composed;

import common.ElementInfo;

public class UnionType extends NamedElementType {
  private UnionSelector selector;
  
  public UnionType(ElementInfo info, UnionSelector selector) {
    super(info);
    this.selector = selector;
  }

  public UnionSelector getSelector() {
    return selector;
  }

  public void setSelector(UnionSelector selector) {
    this.selector = selector;
  }

}
