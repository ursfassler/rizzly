package evl.type.composed;

import common.ElementInfo;

import evl.other.EvlList;

public class UnionType extends NamedElementType {
  final private NamedElement tag;

  public UnionType(ElementInfo info, String name, EvlList<NamedElement> element, NamedElement tag) {
    super(info, name, element);
    this.tag = tag;
  }

  public NamedElement getTag() {
    return tag;
  }

}
