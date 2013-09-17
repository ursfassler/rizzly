package evl.type.composed;

import java.util.Collection;

import common.ElementInfo;

public class UnionType extends NamedElementType {

  private UnionSelector selector;

  public UnionType(ElementInfo info, String name, UnionSelector selector, Collection<NamedElement> element) {
    super(info, name, element);
    this.selector = selector;
  }

  public UnionSelector getSelector() {
    return selector;
  }

  public void setSelector(UnionSelector selector) {
    this.selector = selector;
  }
}
