package evl.type.composed;

import java.util.Collection;

import common.ElementInfo;

public class UnionType extends NamedElementType {
  private NamedElement tag;

  public UnionType(ElementInfo info, String name, Collection<NamedElement> element, NamedElement tag) {
    super(info, name, element);
    this.tag = tag;
  }

  public NamedElement getTag() {
    return tag;
  }

  public void setTag(NamedElement tag) {
    this.tag = tag;
  }

}
