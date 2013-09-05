package fun.type.composed;

import common.ElementInfo;

public class UnionType extends NamedElementType {
  private String selector;
  
  public UnionType(ElementInfo info, String selector) {
    super(info);
    this.selector = selector;
  }

  public String getSelector() {
    return selector;
  }

  public void setSelector(String selector) {
    this.selector = selector;
  }

}
