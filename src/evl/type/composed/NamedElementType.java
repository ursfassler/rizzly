package evl.type.composed;

import common.ElementInfo;

import evl.other.EvlList;
import evl.type.Type;

abstract public class NamedElementType extends Type {
  final private EvlList<NamedElement> element;

  public NamedElementType(ElementInfo info, String name, EvlList<NamedElement> element) {
    super(info, name);
    this.element = element;
  }

  public int getSize() {
    return element.size();
  }

  public EvlList<NamedElement> getElement() {
    return element;
  }

}
