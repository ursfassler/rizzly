package evl.type.base;

import common.ElementInfo;

import evl.other.EvlList;
import evl.type.Type;

public class EnumType extends Type {
  final private EvlList<EnumElement> element = new EvlList<EnumElement>();

  public EnumType(ElementInfo info, String name) {
    super(info, name);
  }

  public EvlList<EnumElement> getElement() {
    return element;
  }

  @Deprecated
  public EnumElement find(String name) {
    return element.find(name);
  }

  public boolean isSupertypeOf(EnumType sub) {
    return element.containsAll(sub.element);
  }

}
